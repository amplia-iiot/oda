package es.amplia.oda.connector.thingstream.mqttsn;

import es.amplia.oda.hardware.atmanager.api.*;

import com.myriadgroup.iot.sdk.IoTSDKConstants;
import com.myriadgroup.iot.sdk.client.modem.IModem;
import com.myriadgroup.iot.sdk.client.modem.IModemCallback;
import com.myriadgroup.iot.sdk.client.modem.ModemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class USSDModemImplementation implements IModem {

    private static final Logger logger = LoggerFactory.getLogger(USSDModemImplementation.class);

    static final long AT_COMMAND_TIMEOUT = 10;

    static final String CMGF_COMMAND = "+CMGF";
    static final String CUSD_COMMAND = "+CUSD";
    static final String CMEE_COMMAND = "+CMEE";

    static final String CSQ_COMMAND = "+CSQ";
    static final String COPS_COMMAND = "+COPS";
    static final String CREG_COMMAND = "+CREG";

    private final ATManager atManager;

    private boolean isRunning = false;

    private final Set<IModemCallback> modemCallbacks = new HashSet<>();

    private final ExecutorService messageExecutorService = Executors.newSingleThreadExecutor();

    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failureCount = new AtomicInteger();


    public USSDModemImplementation(ATManager atManager) {
        this.atManager = atManager;
    }

    @Override
    public synchronized void start() throws ModemException {
        if (isRunning){
            logger.info("AT modem is already started");
            return;
        }

        logger.info("Starting AT modem to send/receive USSD commands");

        try {
            sendCommand(ATCommand.emptyCommand(), true);                                // Hello
            sendCommand(ATCommand.ampersandCommand('F'), true);                         // Restore to default setting
            sendCommand(ATCommand.basicCommand('E', 0), true);                          // Turn off local echo
            sendCommand(ATCommand.sSetCommand(3, 13)      );                            // Mark end of line with CR
            sendCommand(ATCommand.extendedSetCommand(CMGF_COMMAND, "0"));               // Set PDU mode
            sendCommand(ATCommand.extendedSetCommand(CUSD_COMMAND, "1"));               // Accept unsolicited USSD messages
            sendCommand(ATCommand.extendedSetCommand(CMEE_COMMAND, "1"));               // Report mobile equipment error
            sendCommand(ATCommand.extendedCommand(ATCommandType.ACTION, CSQ_COMMAND));  // Signal strength
            sendCommand(ATCommand.extendedCommand(ATCommandType.READ, COPS_COMMAND));   // Operator selection
            sendCommand(ATCommand.extendedCommand(ATCommandType.READ, CREG_COMMAND));   // Network selection

            atManager.registerEvent(CUSD_COMMAND, this::handleCUSDCommandReceived);

            isRunning = true;

            logger.info("AT modem started");
        } catch (Exception e){
            logger.error("Error starting modem: {}", e.getMessage());
            throw new ModemException("An error occurred starting modem", e);
        }
    }

    private synchronized void handleCUSDCommandReceived(ATEvent atCUSDReceived) {
        logger.info("Received CUSD command: {}", atCUSDReceived);

        List<String> parameters = atCUSDReceived.getParameters();
        int code = Integer.parseInt(parameters.get(0));
        String data = parameters.size() > 1 ? parameters.get(1) : null;

        if (code == 4){
            return;
        }

        byte[] bytes;
        try {
            bytes = (data != null) ? data.getBytes(IoTSDKConstants.DEFAULT_ENCODING) : null;
        } catch (UnsupportedEncodingException e) {
            logger.error("Error handling CUSD command {}: Unsupported data encoding", atCUSDReceived);
            return;
        }

        if (!modemCallbacks.isEmpty()) {
            final int finalCode = code;
            final byte[] finalBytes = bytes;
            messageExecutorService.execute(() ->
                    modemCallbacks.forEach(modemCallback -> modemCallback.onReceiveData(finalCode, finalBytes)));
        } else {
            logger.warn("No registered callback, dropping packet");
        }

        logger.info("Received CUSD command processed;");
    }

    @Override
    public synchronized void stop() throws ModemException {
        if (!isRunning){
            logger.info("AT modem is already stopped");
            return;
        }

        try {
            logger.info("Stopping AT modem to send/receive USSD commands");
            sendCommand(ATCommand.extendedSetCommand(CUSD_COMMAND, "2"), true);   // Close USSD session
        } catch (Exception e) {
            logger.error("Error stopping modem: {}", e.getMessage());
            throw new ModemException("An error occurred stopping modem", e);
        } finally{
            atManager.unregisterEvent(CUSD_COMMAND);
            modemCallbacks.clear();
            isRunning = false;
        }

        logger.info("AT modem stopped");
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning;
    }

    @Override
    public synchronized void sendData(byte[] packet) throws ModemException {
        try{
            String dataStr = new String(packet, IoTSDKConstants.DEFAULT_ENCODING);
            ATCommand ussdCmd = ATCommand.extendedSetCommand(CUSD_COMMAND, "1", dataStr);

            logger.info("Sending USSD command: {}", ussdCmd);

            ATResponse atResponse = sendCommand(ussdCmd);

            if (atResponse.getPartialResponses() != null) {
                atResponse.getPartialResponses().forEach(this::handleCUSDCommandReceived);
            }

            successCount.incrementAndGet();
        } catch (Exception e){
            failureCount.incrementAndGet();
            throw new ModemException("An error occurred sending USSD command", e);
        } finally {
            logger.info("Send stats,  success: {}, cusd+4 failures: {}", successCount.get(), failureCount.get());
        }
    }

    @Override
    public synchronized void registerCallback(IModemCallback modemCallback) throws ModemException {
        if (modemCallback == null){
            throw new ModemException("Modem callback is null");
        }

        if (modemCallbacks.contains(modemCallback)){
            logger.warn("Modem callback is already registered: {}", modemCallback);
            return;
        }

        modemCallbacks.add(modemCallback);

        logger.info("Registered callback: {}, {}", modemCallback, modemCallbacks.size());
    }

    @Override
    public synchronized void unregisterCallback(IModemCallback modemCallback) throws ModemException {
        if (modemCallback == null){
            throw new ModemException("Modem callback is null");
        }

        if (!modemCallbacks.contains(modemCallback)){
            logger.warn("Modem callback is not registered: {}", modemCallback);
            return;
        }

        modemCallbacks.remove(modemCallback);

        logger.info("Unregistered callback: {}, {}", modemCallback, modemCallbacks.size());
    }

    @Override
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        ATResponse atResponse;

        try {
            atResponse = sendCommand(ATCommand.extendedCommand(ATCommandType.ACTION, CSQ_COMMAND));  // Signal strength
            status.put("SIGNAL_STRENGTH", String.join(",", atResponse.getPartialResponses().get(0).getParameters()));
            atResponse = sendCommand(ATCommand.extendedCommand(ATCommandType.READ, COPS_COMMAND));   // Operator selection
            status.put("OPERATOR_SELECTION", String.join(",", atResponse.getPartialResponses().get(0).getParameters()));
        }
        catch (Exception e) {
            logger.error("Error occurred getting modem status: {}", e.getMessage());
        }

        return status;
    }

    private ATResponse sendCommand(ATCommand atCommand) throws ModemException {
        return sendCommand(atCommand, false);
    }

    private ATResponse sendCommand(ATCommand atCommand, boolean faultTolerant) throws ModemException {
        logger.debug("Sending command: {}", atCommand);

        try {
            CompletableFuture<ATResponse> future = atManager.send(atCommand, AT_COMMAND_TIMEOUT, TimeUnit.SECONDS);
            ATResponse atResponse = future.get();

            if (!atResponse.isOk() && !faultTolerant) {
                logger.error("Error received with command {}: {}", atCommand, atResponse.getErrorMsg());
                throw new ModemException("Error executing command returned an error: " + atCommand);
            }

            logger.debug("AT command {} processed: {}", atCommand, atResponse);

            return atResponse;
        } catch (Exception e) {
            if (!faultTolerant) {
                throw new ModemException("Error executing command returned an error: " + atCommand);
            } else {
                return ATResponse.error(e.getMessage());

            }
        }
    }
}
