package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static es.amplia.oda.datastreams.deviceinfofx30.ScriptsLoaderImpl.DELETE_DIR_COMMAND;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ScriptsLoaderImplTest {

    @Mock
    private CommandProcessor mockedCommandProcessor;
    @InjectMocks
    private ScriptsLoaderImpl testScriptsLoader;

    @Captor
    private ArgumentCaptor<String> commandCaptor;

    @Test
    public void testLoadScripts() {

    }

    @Test
    public void testClose() throws CommandExecutionException {
        testScriptsLoader.close();

        verify(mockedCommandProcessor).execute(commandCaptor.capture());
        String command = commandCaptor.getValue();
        assertTrue(command.contains(DELETE_DIR_COMMAND));
    }
}