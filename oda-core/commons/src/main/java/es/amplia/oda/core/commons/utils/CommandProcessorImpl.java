package es.amplia.oda.core.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandProcessorImpl implements CommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorImpl.class);

    @Override
    public String execute(String command) throws CommandExecutionException {
        return execute(command, 5000);
    }

    @Override
    public String execute(String command, long timeoutMillis) throws CommandExecutionException {
        Process processCommand = null;
        try {
            processCommand = Runtime.getRuntime().exec(command);
            if (!processCommand.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                String message = "Timeout (" + timeoutMillis + " ms) reached executing command " + command;
                LOGGER.error(message);
                processCommand.destroy();
                throw new CommandExecutionException(command, message, new TimeoutException());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(processCommand.getInputStream()));
            String result = reader.readLine();
            reader.close();

            return result;
        } catch (IOException|InterruptedException exception) {
            String message = "Error executing command " + command;
            LOGGER.error(message);
            if (processCommand != null) processCommand.destroy();
            throw new CommandExecutionException(command, message, exception);
        }
    }
}
