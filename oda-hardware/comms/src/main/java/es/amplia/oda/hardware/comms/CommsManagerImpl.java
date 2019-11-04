package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class CommsManagerImpl implements CommsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommsManagerImpl.class);
    static final String INIT_SIM_SCRIPT = "/scripts/initSim.sh";
    static final String CONFIGURE_CONNECTION_SCRIPT = "/scripts/configureConnection.sh";
    static final String CONNECT_SCRIPT = "/scripts/connect.sh";


    private final CommandProcessor commandProcessor;
    private final ResourceManager resourceManager;
    private final ScheduledExecutorService executor;
    private Thread connectionThread;
    private ScheduledFuture<?> scheduledConnection;


    CommsManagerImpl(CommandProcessor commandProcessor, ResourceManager resourceManager,
                     ScheduledExecutorService executor) {
        this.commandProcessor = commandProcessor;
        this.resourceManager = resourceManager;
        this.executor = executor;
    }

    @Override
    public void connect(String pin, String apn, String username, String password, int connectionTimeout, long retryConnectionTimer) {
        try {
            clear();
            initSim(pin);
            configureConnection(apn, username, password);
            connect(connectionTimeout, retryConnectionTimer);
        } catch (CommandExecutionException e) {
            LOGGER.error("Error establishing communications with SIM PIN {}, APN {}, username {} and password {}",
                    pin, apn, username, password, e);
        }
    }

    private void initSim(String pin) throws CommandExecutionException {
        executeScript(INIT_SIM_SCRIPT, pin);
    }

    private void executeScript(String scriptName, String ... params) throws CommandExecutionException {
        commandProcessor.execute(resourceManager.getResourcePath(scriptName) + " " + String.join(" ", params));
    }

    private void configureConnection(String apn, String username, String password) throws CommandExecutionException {
        executeScript(CONFIGURE_CONNECTION_SCRIPT, apn, username, password);
    }

    private void connect(int connectionTimeout, long retryConnectionTimer) {
        connectionThread = new Thread(() -> {
            try {
                executeScript(CONNECT_SCRIPT, Integer.toString(connectionTimeout));
            } catch (CommandExecutionException e) {
                LOGGER.error("Error establishing data connection", e);
                scheduledConnection =
                        executor.schedule(() -> connect(connectionTimeout, retryConnectionTimer), retryConnectionTimer, TimeUnit.SECONDS);
            }
        });
        connectionThread.start();
    }

    private void clear() {
        Optional.ofNullable(scheduledConnection).ifPresent(sc -> sc.cancel(false));
        Optional.ofNullable(connectionThread).ifPresent(Thread::interrupt);
    }

    @Override
    public void close() {
        clear();
    }
}
