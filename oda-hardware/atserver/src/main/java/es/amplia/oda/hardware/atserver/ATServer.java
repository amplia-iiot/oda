package es.amplia.oda.hardware.atserver;

import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.hardware.atmanager.ATManagerImpl;
import es.amplia.oda.hardware.atmanager.ATParserImpl;
import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atmanager.api.ATResponse;

import es.amplia.oda.hardware.atserver.configuration.ATServerConfiguration;
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

public class ATServer implements ATManager, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ATServer.class);

    private final ServiceRegistrationManager<ATManager> atManagerRegistrationManager;

    private SerialPort commPort;
    private ATManager atManager;

    ATServer(ServiceRegistrationManager<ATManager> atManagerRegistrationManager) {
        this.atManagerRegistrationManager = atManagerRegistrationManager;
    }

    public void loadConfiguration(ATServerConfiguration configuration) {
        try {
            close();

            commPort = (SerialPort) CommPortIdentifier.getPortIdentifier(configuration.getPortName())
                                        .open(configuration.getAppName(), configuration.getTimeToGetPort());
            if (commPort == null) {
                throw new IllegalArgumentException("Cannot open " + configuration.getPortName() + " as AT comm port");
            }
            commPort.setSerialPortParams(configuration.getBaudRate(), configuration.getDataBits(),
                    configuration.getStopBits(), configuration.getParity());
            LOGGER.info("Opened {} as AT comm port {} {}{}{}", configuration.getPortName(), configuration.getBaudRate(),
                    configuration.getDataBits(), configuration.getParity(), configuration.getStopBits());
            LOGGER.info("Time between commands configured {}", configuration.getTimeBetweenCommands());

            atManager = new ATManagerImpl(new ATParserImpl(), commPort.getOutputStream(),
                    configuration.getTimeBetweenCommands());

            commPort.addEventListener(this::processSerialPortEvent);
            commPort.notifyOnDataAvailable(true);

            atManagerRegistrationManager.register(atManager);
        } catch (NoSuchPortException e) {
            close();
            throw new IllegalArgumentException("No such port: " + configuration.getPortName());
        } catch (PortInUseException e) {
            close();
            throw new IllegalArgumentException("Port already in use: " + configuration.getPortName());
        } catch (UnsupportedCommOperationException e) {
            close();
            throw new IllegalArgumentException("Invalid configuration " + configuration + " of port " +
                    configuration.getPortName());
        } catch (IOException e) {
            close();
            throw new IllegalArgumentException("Error getting output stream");
        } catch (TooManyListenersException e) {
            close();
            throw new IllegalArgumentException("Too many listeners");
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
