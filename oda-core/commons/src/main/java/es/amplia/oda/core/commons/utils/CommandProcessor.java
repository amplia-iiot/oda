package es.amplia.oda.core.commons.utils;

public interface CommandProcessor {
    String execute(String command) throws CommandExecutionException;

    String execute(String command, long timeoutMillis) throws CommandExecutionException;
}
