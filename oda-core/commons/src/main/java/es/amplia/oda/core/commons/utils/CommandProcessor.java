package es.amplia.oda.core.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility to execute commands in the environment runtime.
 */
public class CommandProcessor {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);

    /**
     * Private constructor to avoid the class instantiation.
     */
    private CommandProcessor() {}

    /**
     * Execute the given command.
     * @param command Command to execute.
     * @return Result of the command.
     * @throws CommandExecutionException Exception executing the command.
     */
    public static String execute(String command) throws CommandExecutionException {
    	return execute(command, 5000);
    }
    
    /**
     * Execute the given command.
     * @param command Command to execute.
     * @param timeoutMillis Timeout in milliseconds for executing the command.
     * @return Result of the command.
     * @throws CommandExecutionException Exception executing the command.
     */
    public static String execute(String command, long timeoutMillis) throws CommandExecutionException {
    	Process processCommand = null;
        try {
            processCommand = Runtime.getRuntime().exec(command);
            if (!processCommand.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
            	String message = "Timeout (" + timeoutMillis + " ms) reached executing command " + command;
                logger.error(message);
                processCommand.destroy();
            	throw new CommandExecutionException(command, message, new TimeoutException());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(processCommand.getInputStream()));
            String result = reader.readLine();
            reader.close();

            return result;
        } catch (IOException|InterruptedException exception) {
            String message = "Error executing command " + command;
            logger.error(message);
            if (processCommand != null) processCommand.destroy();
            throw new CommandExecutionException(command, message, exception);
        }
    }

}
