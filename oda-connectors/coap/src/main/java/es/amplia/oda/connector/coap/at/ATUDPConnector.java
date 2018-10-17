package es.amplia.oda.connector.coap.at;

import es.amplia.oda.hardware.atmanager.api.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ATUDPConnector implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(ATUDPConnector.class);

    static final long COMMAND_TIMEOUT = 10;
    static final String OPEN_SOCKET_AT_COMMAND = "+NSOCR";
    static final String DGRAM_TYPE = "DGRAM";
    static final int UDP_TYPE = 17;
    static final String GET_ADDRESS_AT_COMMAND = "+CGPADDR";
    static final String SEND_MESSAGE_AT_COMMAND = "+NSOST";
    static final String ARRIVE_SOCKET_MESSAGE_AT_EVENT = "+NSONMI";
    static final String READ_MESSAGE_AT_COMMAND = "+NSORF";
    static final String CLOSE_SOCKET_AT_COMMAND = "+NSOCL";

    private static final Pattern OPEN_SOCKET_RESPONSE_PATTERN = Pattern.compile("[\\s\\r\\n]*(\\d+)[\\s\\r\\n]*");
    private static final Pattern READ_MESSAGE_RESPONSE_PATTERN =
            Pattern.compile("[\\s\\r\\n]*\\d+,\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3},\\d+,\\d+,([A-Fa-f0-9]+),\\d+[\\s\\r\\n]*");

    private final ATManager atManager;
    private final String remoteHost;
    private final int remotePort;
    private final int localPort;

    private RawDataChannel receiver;

    private int localSocketId;
    private InetSocketAddress localAddress;

    private ExecutorService senderExecutor;
    private ExecutorService receiverExecutor;

    private volatile boolean running;


    public ATUDPConnector(ATManager atManager, String remoteHost, int remotePort, int localPort) {
        this.atManager = atManager;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.localPort = localPort;
    }


    @Override
    public synchronized void start() {
        if (running) {
            return;
        }

        try {
            closeSocket();
            localSocketId = createLocalSocket();
            localAddress = getLocalAddress();

            if (localSocketId == -1 || localAddress == null) {
                logger.error("Can not start the AT UPD connector");
                return;
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Can not start the AT UDP connector");
            return;
        }

        try {
            atManager.registerEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT, this::processArriveSocketMessageEvent);
        } catch (ATManager.AlreadyRegisteredException e) {
            logger.warn("AT event {} already registered", ARRIVE_SOCKET_MESSAGE_AT_EVENT);
        }

        senderExecutor = Executors.newSingleThreadExecutor();
        receiverExecutor = Executors.newSingleThreadExecutor();

        running = true;
    }

    private int createLocalSocket() throws InterruptedException, ExecutionException {
        CompletableFuture<ATResponse> future =
                atManager.send(ATCommand.extendedSetCommand(OPEN_SOCKET_AT_COMMAND, DGRAM_TYPE,
                        String.valueOf(UDP_TYPE), String.valueOf(localPort)), COMMAND_TIMEOUT, TimeUnit.SECONDS);
        ATResponse response = future.get();

        if (response.isOk()) {
            Matcher createSocketResponseMatcher = OPEN_SOCKET_RESPONSE_PATTERN.matcher(response.getBody());
            if (createSocketResponseMatcher.matches()) {
                return Integer.parseInt(createSocketResponseMatcher.group(1));
            } else {
                logger.warn("Invalid response to open socket command");
            }
        } else {
            logger.warn("Error executing create local socket command: {}", response.getErrorMsg());
        }

        return -1;
    }

    private InetSocketAddress getLocalAddress() throws InterruptedException, ExecutionException {
        CompletableFuture<ATResponse> future =
                atManager.send(ATCommand.extendedCommand(ATCommandType.ACTION, GET_ADDRESS_AT_COMMAND),
                        COMMAND_TIMEOUT, TimeUnit.SECONDS);
        ATResponse response = future.get();

        if (response.isOk()) {
            List<ATEvent> partialResponses = response.getPartialResponses();
            if (partialResponses != null && !partialResponses.isEmpty()) {
                ATEvent socketIdResponse = partialResponses.get(0);

                if (socketIdResponse.getParameters().size() == 2) {
                    String ip = socketIdResponse.getParameters().get(1);
                    return new InetSocketAddress(ip, localPort);
                } else {
                    logger.warn("Invalid response to get address command");
                }
            } else {
                logger.warn("Invalid response to get address command");
            }
        } else {
            logger.warn("Error executing get address command: {}", response.getErrorMsg());
        }

        return null;
    }

    void processArriveSocketMessageEvent(ATEvent atEvent) {
        List<String> parameters = atEvent.getParameters();

        if (parameters.size() != 2) {
            logger.warn("Invalid parameters for Arrived socket message event");
            return;
        }

        try {
            int receivedSocketId = Integer.parseInt(parameters.get(0));
            int receivedMessageId = Integer.parseInt(parameters.get(1));

            receiverExecutor.submit(() -> readArrivedMessage(receivedSocketId, receivedMessageId));
        } catch (NumberFormatException e) {
            logger.warn("Invalid parameters for Arrived socket message event");
        }
    }

    void readArrivedMessage(int socketId, int messageId) {
        String msg = readMessage(socketId, messageId);

        if (msg == null) {
            logger.warn("Error reading receive data in message {} from socket {}", messageId, socketId);
            return;
        }

        try {
            byte[] bytes = Hex.decodeHex(msg);
            RawData rawData = new RawData(bytes, InetAddress.getByName(remoteHost), remotePort);

            receiver.receiveData(rawData);
        } catch (DecoderException | UnknownHostException e) {
            logger.warn("Error reading receive data in message {} from socket {}", messageId, socketId);
        }
    }

    private String readMessage(int socketId, int messageId) {
        ATCommand readMessage = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));
        CompletableFuture<ATResponse> future = atManager.send(readMessage, COMMAND_TIMEOUT, TimeUnit.SECONDS);

        try {
            ATResponse response = future.get();

            if (response.isOk()) {
                logger.info("Message {} read from socket {}", messageId, socketId);

                Matcher readResponseMatcher = READ_MESSAGE_RESPONSE_PATTERN.matcher(response.getBody());
                if (readResponseMatcher.matches()) {
                    return readResponseMatcher.group(1);
                } else {
                    logger.warn("Invalid response to read message command");
                }
            } else {
                logger.info("Error reading message {} from socket {}: {}", messageId, socketId,
                        response.getErrorMsg());
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.warn("Error reading message {} from socket {}: {}", messageId, socketId, e.getMessage());
        }

        return null;
    }

    @Override
    public void send(RawData msg) {
        if (!running) {
            logger.warn("AT UDP connector is not running");
        } else if (msg == null) {
            logger.warn("Message must not be null");
        } else {
            String data = Hex.encodeHexString(msg.getBytes()).toUpperCase();
            senderExecutor.submit(() -> sendMessage(localSocketId, remoteHost, remotePort, msg.getSize(), data));
        }
    }

    /**
     * Send the given message through the socket with the given socket id.
     * @param socketId Socket id where to send the data
     * @param host Host
     * @param port Port
     * @param length Data length
     * @param msg Data in hexadecimal format. Important: Should be upper case
     */
    void sendMessage(int socketId, String host, int port, int length, String msg) {
        ATCommand sendMessageCommand = ATCommand.extendedSetCommand(SEND_MESSAGE_AT_COMMAND,
                String.valueOf(socketId), host, String.valueOf(port), String.valueOf(length), msg);
        CompletableFuture<ATResponse> future =
                atManager.send(sendMessageCommand, COMMAND_TIMEOUT, TimeUnit.SECONDS);

        try {
            ATResponse response = future.get();

            if (response.isOk()) {
                logger.info("Message sent through socket {}", socketId);
            } else {
                logger.info("Error sending message through socket {}: {}", socketId, response.getErrorMsg());
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.warn("Error sending message through socket {}: {}", socketId, e.getMessage());
        }
    }

    @Override
    public void setRawDataReceiver(RawDataChannel receiver) {
        this.receiver = receiver;
    }

    @Override
    public InetSocketAddress getAddress() {
        return localAddress;
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }

        closeSocket();

        atManager.unregisterEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT);

        senderExecutor.shutdown();
        receiverExecutor.shutdown();

        localAddress = null;
        running = false;
    }

    private void closeSocket() {
        ATCommand closeSocketCommand = ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        CompletableFuture<ATResponse> future = atManager.send(closeSocketCommand, COMMAND_TIMEOUT, TimeUnit.SECONDS);

        try {
            ATResponse response = future.get();

            if (response.isOk()) {
                logger.info("Socket {} closed", localSocketId);
            } else {
                logger.info("Error closing socket {}: {}", localSocketId, response.getErrorMsg());
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error closing socket {}: {}", localSocketId, e.getMessage());
        }
    }

    @Override
    public void destroy() {
        stop();
    }
}
