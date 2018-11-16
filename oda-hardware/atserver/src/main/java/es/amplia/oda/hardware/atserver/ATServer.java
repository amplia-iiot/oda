package es.amplia.oda.hardware.atserver;

import es.amplia.oda.hardware.atmanager.ATManagerImpl;
import es.amplia.oda.hardware.atmanager.ATParserImpl;
import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atmanager.api.ATResponse;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

class ATServer implements ATManager, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ATServer.class);

    private SerialPort commPort;
    private ATManager atManager;

    void loadConfiguration(ATServerConfiguration configuration) throws ConfigurationException {
        try {
            close();

            commPort = (SerialPort) CommPortIdentifier.getPortIdentifier(configuration.getPortName())
                                        .open(configuration.getAppName(), configuration.getMillisecondsToGetPort());
            if (commPort == null) {
                LOGGER.error("Cannot open {} as AT comm port", configuration.getPortName());
                throw new ConfigurationException("port-name", "Cannot open as AT comm port");
            }
            commPort.setSerialPortParams(configuration.getBaudRate(), configuration.getDataBits(),
                    configuration.getStopBits(), configuration.getParity());
            LOGGER.info("Opened {} as AT comm port {} {}{}{}", configuration.getPortName(), configuration.getBaudRate(),
                    configuration.getDataBits(), configuration.getParity(), configuration.getStopBits());

            atManager = new ATManagerImpl(new ATParserImpl(), commPort.getOutputStream());

            commPort.addEventListener(this::processSerialPortEvent);
            commPort.notifyOnDataAvailable(true);
        } catch (NoSuchPortException e) {
            LOGGER.error("", e);
            close();
            throw new ConfigurationException("port-name", "No such port");
        } catch (PortInUseException e) {
            LOGGER.error("", e);
            close();
            throw new ConfigurationException("port-name", "Port already in use");
        } catch (UnsupportedCommOperationException e) {
            LOGGER.error("", e);
            close();
            throw new ConfigurationException("port-name", "The combination of baud-rate, data-bits, stop-bits and parity is not supported");
        } catch (IOException e) {
            LOGGER.error("", e);
            close();
            throw new ConfigurationException("port-name", "Error getting output stream");
        } catch (TooManyListenersException e) {
            LOGGER.error("", e);
            close();
            throw new ConfigurationException("port-name", "Too many listeners");
        }
    }

    private void processSerialPortEvent(SerialPortEvent event) {
        try {
            if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) return;
            InputStream input = commPort.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
            while (buffer.ready()) {
                String line = buffer.readLine();
                LOGGER.info("Received in comm port: \"{}\"", line);
                atManager.process(line);
            }
        } catch (IOException e) {
            LOGGER.error("Error processing evet {}: {}", event, e);
        }
    }

    @Override
    public void registerEvent(String event, Consumer<ATEvent> eventHandler) throws AlreadyRegisteredException {
        atManager.registerEvent(event, eventHandler);
    }

    @Override
    public void unregisterEvent(String event) {
        atManager.unregisterEvent(event);
    }

    @Override
    public void registerCommand(String command, Function<ATCommand, ATResponse> commandHandler)
            throws AlreadyRegisteredException {
        atManager.registerCommand(command, commandHandler);
    }

    @Override
    public void unregisterCommand(String command) {
        atManager.unregisterCommand(command);
    }

    @Override
    public void process(String line) {
        atManager.process(line);
    }

    @Override
    public CompletableFuture<ATResponse> send(ATCommand command, long timeout, TimeUnit unit) {
        return atManager.send(command, timeout, unit);
    }

    @Override
    public void send(ATEvent event) {
        atManager.send(event);
    }

    @Override
    public void close() {
        if (commPort != null) {
            commPort.close();
            commPort = null;
        }
    }
}
