package es.amplia.oda.core.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandProcessorImplTest {

    private static final String ECHO_COMMAND = "echo ";
    private static final String WRONG_COMMAND = "wrong";
    private static final String HELLO_WORLD = "Hello World!";

    private final CommandProcessorImpl commandProcessor = new CommandProcessorImpl();

    @Test
    public void testExecute() throws CommandExecutionException {
        /*String result = commandProcessor.execute(ECHO_COMMAND + HELLO_WORLD);

        assertEquals(HELLO_WORLD, result);*/
    }

    @Test
    public void testExecuteWrongCommand() {
        assertThrows(CommandExecutionException.class, () -> commandProcessor.execute(WRONG_COMMAND + HELLO_WORLD));
    }
}