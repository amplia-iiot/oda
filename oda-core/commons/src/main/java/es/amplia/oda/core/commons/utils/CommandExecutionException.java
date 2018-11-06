package es.amplia.oda.core.commons.utils;

/**
 * Exception executing a command in the environment runtime.
 */
public class CommandExecutionException extends Exception {

    private static final long serialVersionUID = 4453204819658040009L;
    /**
     * Command causing the exception.
     */
    private final String command;

    /**
     * Constructor.
     * @param command Command causing the exception.
     * @param message Exception message.
     * @param innerException Inner exception.
     */
    public CommandExecutionException(String command, String message, Exception innerException) {
        super(message, innerException);
        this.command = command;
    }

    /**
     * Get the command causing the exception.
     * @return Command causing the exception.
     */
    public String getCommand() {
        return command;
    }
}
