package es.amplia.oda.core.commons.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandExecutionExceptionTest {

    private static final String TEST_COMMAND = "command";
    private static final String TEST_MESSAGE = "Test message";

    @Test
    public void testGetCommand() {
        CommandExecutionException exception = new CommandExecutionException(TEST_COMMAND, TEST_MESSAGE, null);

        assertEquals(TEST_COMMAND, exception.getCommand());
    }
}