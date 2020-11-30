package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

class CommsManagerImpl implements CommsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommsManagerImpl.class);
    static final String INIT_SIM_SCRIPT = "/initSim.sh";
    static final String CONFIGURE_CONNECTION_SCRIPT = "/configureConnection.sh";
    static final String CONNECT_SCRIPT = "/connect.sh";
    static final String CHECK_APN_SCRIPT = "/checkApn.sh";
    static final String SIM_STATUS_SCRIPT = "/simStatus.sh";
    static final String GET_APN_SCRIPT = "/getApn.sh";
    static final long COMMAND_TIMEOUT_MS = 60000;


    private final CommandProcessor commandProcessor;
    private Thread connectionThread = null;
    private boolean needToRedirect;
    private String apn;
    private String pin;
    private String path;
    private boolean reconnect = false;

    CommsManagerImpl(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }

    @Override
    public void connect(String pin, String apn, String username, String password, int connectionTimeout, long retryConnectionTimer, String path) {
        try {
            close();
            this.path = path;
            this.pin = pin;
            if(!pin.isEmpty()) {
                initSim(pin);
            }
            this.reconnect = true;
            LOGGER.info("SIM initialized");
            this.apn = apn;
            checkApnToRestoreIfChanged();
            if (!needToRedirect) {
                configureConnection(username, password);
                LOGGER.info("Connection configured");
                connect(connectionTimeout, retryConnectionTimer);
                LOGGER.info("Achieved the GSM connection with APN {}", apn);
            }
        } catch (CommandExecutionException e) {
            LOGGER.error("Error establishing communications with SIM PIN {}, APN {}, username {} and password {}",
                    pin, apn, username, password, e);
            if(!reconnect) {
                LOGGER.error("SIM card can not be initialied (SIM no present or PIN number is not correct). Can't start mobile communications.");
            }
        }
    }

    private void initSim(String pin) throws CommandExecutionException {
        executeScript(INIT_SIM_SCRIPT, pin);
    }

    private String executeScript(String scriptName, String ... params) throws CommandExecutionException {
        return executeScript(scriptName, COMMAND_TIMEOUT_MS, params);
    }

    private String executeScript(String scriptName, long commandTimeout, String ... params)
            throws CommandExecutionException {
        String command = this.path + scriptName;
        LOGGER.debug("Executing command {} with params: {}", command, params);
        return commandProcessor.execute(command + " " + String.join(" ", params),
                commandTimeout);
    }

    private void configureConnection(String username, String password) throws CommandExecutionException {
        executeScript(CONFIGURE_CONNECTION_SCRIPT, apn, username, password);
    }

    private void connect(int connectionTimeout, long retryConnectionTimer) {
        connectionThread = new Thread(() -> {
            boolean executeConnectCommand = true;
            while (true) {
                try {
                    if (executeConnectCommand) {
                        tryToConnect(connectionTimeout);
                    }
                } catch (CommandExecutionException e) {
                    LOGGER.error("Error establishing connection.", e);
                }

                if (reconnect) {
                    executeConnectCommand = waitToReconnect(executeConnectCommand, COMMAND_TIMEOUT_MS + retryConnectionTimer * 1000);
                } else break;
            }
        });
        connectionThread.start();
    }

    private void tryToConnect(int connectionTimeout) throws CommandExecutionException {
        LOGGER.debug("Connecting mobile data communications");
        String actualApn = executeScript(GET_APN_SCRIPT, COMMAND_TIMEOUT_MS);
        if (!needToRedirect && !apn.equals(actualApn)) {
            LOGGER.info("Reconfiguring APN after a unexpected change: Expected APN is {} and found APN is {}", apn, actualApn);
            checkApnToRestoreIfChanged();
        }

        String result = executeScript(CONNECT_SCRIPT, COMMAND_TIMEOUT_MS + connectionTimeout * 1000, Integer.toString(connectionTimeout));
        if (result != null && !"".equals(result)) {
            LOGGER.debug("Command response: {}", result);
        }

        LOGGER.debug("Command connect finnished");
    }

    private boolean waitToReconnect(boolean executeConnectCommand, long retryConnectionTimer) {
        LOGGER.debug("Retrying connection in {} milliseconds", retryConnectionTimer);
        try {
            Thread.sleep(retryConnectionTimer);
            if (!isSimFullInitialized(pin)) {
                // If SIM needs initialize again and fails, try again without execute connect command
                LOGGER.info("SIM card can not be initialied (SIM not present or PIN number is incorrect). Can't start mobile communications.");
                executeConnectCommand = false;
            } else executeConnectCommand = true;
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted thread during the retry connection wait", e);
            Thread.currentThread().interrupt();
        } catch (CommandExecutionException e) {
            LOGGER.error("Error during the execution of SIM checking", e);
        }
        return executeConnectCommand;
    }

    private boolean isSimFullInitialized(String pin) throws CommandExecutionException {
        String response;

        // Command "cm sim" for knowing if SIM is present
        response = executeScript(SIM_STATUS_SCRIPT);
        if (!response.contains("ABSENT")) {
            if (response.contains(" locked")) {
                // Command "cm sim enterpin <pin>" for entering SIM PIN
                LOGGER.info("SIM card needs PIN, using PIN number: {}", pin);
                initSim(pin);
            }
            return true;
        }
        return false;
    }

    private void checkApnToRestoreIfChanged() throws CommandExecutionException {
        LOGGER.debug("Checking for APN {}", apn);
        String result = executeScript(CHECK_APN_SCRIPT, COMMAND_TIMEOUT_MS, apn, path);
        LOGGER.debug("Setting APN result: {}", result);
        if (result == null || result.equals("")) {
            needToRedirect = false;
        } else {
            needToRedirect = "REDIRECTING".equals(result);
        }
        String log = needToRedirect ? "won't be refreshed until the system is rebooted" : "will be checked and refreshed again if it is needed";
        LOGGER.debug("Setted the APN to {}. APN {}", apn, log);
    }

    private void clear() {
        Optional.ofNullable(connectionThread).ifPresent(Thread::interrupt);
        connectionThread = null;
    }

    @Override
    public void close() {
        this.reconnect = false;
        clear();
    }
}
