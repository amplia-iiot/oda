package es.amplia.oda.datastreams.deviceinfo.configuration;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScriptsLoader.class)
public class ScriptsLoaderTest {
	@Mock
	private CommandProcessor mockedCommandProcessor;
	@InjectMocks
	private ScriptsLoader scriptsLoader;
	@Mock
	private File mockedFile;
	@Mock
	private JarFile mockedJarFile;
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
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		whenNew(JarFile.class).withAnyArguments().thenReturn(mockedJarFile);
		whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fos);
		File[] files = new File[1];
		files[0] = mockedFile;
		when(mockedFile.listFiles()).thenReturn(files);
		when(mockedFile.getName()).thenReturn("es.amplia.oda.datastreams.deviceinfo");
		when(mockedCommandProcessor.execute(contains("mkdir"))).thenReturn(null);
		when(mockedCommandProcessor.execute(contains("cp"))).thenReturn(null);
		when(mockedJarFile.entries()).thenReturn(mockedEnumeration);
		when(mockedEnumeration.hasMoreElements()).thenReturn(true, false);
		when(mockedEnumeration.nextElement()).thenReturn(mockedJarEntry);
		when(mockedJarEntry.getName()).thenReturn(".sh");
		when(mockedJarFile.getInputStream(any())).thenReturn(mockedInputStream);
		when(mockedInputStream.available()).thenReturn(1, 0);
		when(mockedInputStream.read()).thenReturn(0);

		scriptsLoader.load("tests");

		assertTrue(result.length() > init);
	}

	@Test(expected = CommandExecutionException.class)
	public void testLoadWithNoJarToExtract() throws Exception {
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		File[] files = new File[1];
		files[0] = mockedFile;
		when(mockedFile.listFiles()).thenReturn(files);
		when(mockedFile.getName()).thenReturn("another.package");

		scriptsLoader.load("tests");
	}

	@Test
	public void testClose() throws Exception {
		when(mockedCommandProcessor.execute(any())).thenReturn(null);

		scriptsLoader.close();

		verify(mockedCommandProcessor, times(1)).execute(any());
	}
}
