package es.amplia.oda.core.commons.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
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
    public void testClose() throws CommandExecutionException, IOException {
        File testDir = new File(System.getProperty("user.dir") + "/testing");
        File testFile = new File(System.getProperty("user.dir") + "/testing/test");
        testDir.mkdir();
        testFile.createNewFile();
        Whitebox.setInternalState(testScriptsLoader, "path", testDir.getPath());
        testScriptsLoader.close();

        testFile.delete();
        testDir.delete();
        verify(mockedCommandProcessor).execute(commandCaptor.capture());
        String command = commandCaptor.getValue();
        assertTrue(command.contains(ScriptsLoaderImpl.DELETE_DIR_COMMAND));
    }
}