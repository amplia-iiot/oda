package es.amplia.oda.datastreams.deviceinfoowa450.configuration;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ScriptsLoaderTest {
	@Mock
	private CommandProcessor mockedCommandProcessor;
	@InjectMocks
	private ScriptsLoader scriptsLoader;
	@Mock
	private Enumeration<JarEntry> mockedEnumeration;
	@Mock
	private JarEntry mockedJarEntry;
	@Mock
	private InputStream mockedInputStream;

	@Test
	public void testLoad() throws Exception {
		File result = new File("temp");
		result.createNewFile();
		FileOutputStream fos = new FileOutputStream(result);
		long init = result.length();
		try (MockedConstruction<File> fileCons = mockConstruction(File.class,
				(mock, mctx) -> {
					when(mock.listFiles()).thenReturn(new File[] { mock });
					when(mock.getName()).thenReturn("es.amplia.oda.datastreams.deviceinfo");
				});
			 MockedConstruction<JarFile> jarFileCons = mockConstruction(JarFile.class,
				(mock, mctx) -> {
					when(mock.entries()).thenReturn(mockedEnumeration);
					when(mock.getInputStream(any())).thenReturn(mockedInputStream);
				});
			 MockedConstruction<FileOutputStream> fosCons = mockConstruction(FileOutputStream.class,
				(mock, mctx) -> doAnswer(invocation -> {
					fos.write((int) invocation.getArgument(0));
					return null;
				}).when(mock).write(anyInt()))) {
			when(mockedCommandProcessor.execute(contains("mkdir"))).thenReturn(null);
			when(mockedCommandProcessor.execute(contains("cp"))).thenReturn(null);
			when(mockedEnumeration.hasMoreElements()).thenReturn(true, false);
			when(mockedEnumeration.nextElement()).thenReturn(mockedJarEntry);
			when(mockedJarEntry.getName()).thenReturn(".sh");
			when(mockedInputStream.available()).thenReturn(1, 0);
			when(mockedInputStream.read()).thenReturn(0);

			scriptsLoader.load("deploy", "tests");
		}
		fos.close();

		assertTrue(result.length() > init);
		result.delete();
	}

	@Test(expected = CommandExecutionException.class)
	public void testLoadWithNoJarToExtract() throws Exception {
		try (MockedConstruction<File> fileCons = mockConstruction(File.class,
				(mock, mctx) -> {
					when(mock.listFiles()).thenReturn(new File[] { mock });
					when(mock.getName()).thenReturn("another.package");
				})) {
			scriptsLoader.load("deploy", "tests");
		}
	}

	@Test
	public void testClose() throws Exception {
		when(mockedCommandProcessor.execute(any())).thenReturn(null);

		scriptsLoader.close();

		verify(mockedCommandProcessor, times(1)).execute(any());
	}
}
