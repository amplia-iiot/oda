package es.amplia.oda.core.commons.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CommandProcessorImplTest {

    private static final String ECHO_COMMAND = "echo ";
    private static final String WRONG_COMMAND = "wrong";
    private static final String HELLO_WORLD = "Hello World!";

    private final CommandProcessorImpl commandProcessor = new CommandProcessorImpl();

    @Test
    public void testExecute() throws CommandExecutionException {
        String result = commandProcessor.execute(ECHO_COMMAND + HELLO_WORLD);

        assertEquals(HELLO_WORLD, result);
    }

    @Test(expected = CommandExecutionException.class)
    public void testExecuteWrongCommand() throws CommandExecutionException {
        commandProcessor.execute(WRONG_COMMAND + HELLO_WORLD);

        fail("Command execution exception must be thrown");
    }
}