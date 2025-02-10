package es.amplia.oda.datastreams.deviceinfo.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScriptsLoader.class)
public class ScriptsLoaderTest {

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

	String sourceDir = "tempSource";
	String destDir = "tempDest";


	@Before
	public void start() throws IOException {
		Path sourceDirPath = Paths.get(sourceDir);
		Path destDirPath = Paths.get(destDir);

		if (Files.exists(sourceDirPath)) {
			File dir = new File(String.valueOf(sourceDirPath));
			for (File insideDirFile : Objects.requireNonNull(dir.listFiles())) {
				Files.delete(Paths.get(insideDirFile.getPath()));
			}
			Files.delete(sourceDirPath);
		}
		if (Files.exists(destDirPath)) {
			File dir = new File(String.valueOf(destDirPath));
			for (File insideDirFile : Objects.requireNonNull(dir.listFiles())) {
				Files.delete(Paths.get(insideDirFile.getPath()));
			}
			Files.delete(destDirPath);
		}

		Files.createDirectories(sourceDirPath);
		Files.createDirectories(destDirPath);
	}

	@After
	public void end() throws IOException {
		Path sourceDirPath = Paths.get(sourceDir);
		Path destDirPath = Paths.get(destDir);

		if (Files.exists(sourceDirPath)) {
			File dir = new File(String.valueOf(sourceDirPath));
			File[] filesInDir = dir.listFiles();
			if (filesInDir != null) {
				for (File insideDirFile : filesInDir) {
					if (insideDirFile == mockedFile) {
						continue;
					}
					Files.delete(Paths.get(insideDirFile.getPath()));
				}
			}
			Files.delete(sourceDirPath);
		}
		if (Files.exists(destDirPath)) {
			File dir = new File(String.valueOf(destDirPath));
			File[] filesInDir = dir.listFiles();
			if (filesInDir != null) {
				for (File insideDirFile : filesInDir) {
					if (insideDirFile == mockedFile) {
						continue;
					}
					Files.delete(Paths.get(insideDirFile.getPath()));
				}
			}
			Files.delete(destDirPath);
		}
	}

	@Test
	public void testLoad() throws Exception {

		File resultFile = new File(sourceDir + File.separator + "temp");
		Files.createFile(resultFile.toPath());

		//
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		when(mockedFile.exists()).thenReturn(false);
		File[] files = new File[1];
		files[0] = mockedFile;
		when(mockedFile.listFiles()).thenReturn(files);

		//
		when(mockedFile.getName()).thenReturn("es.amplia.oda.datastreams.deviceinfo");

		//
		when(mockedFile.toPath()).thenReturn(Paths.get(sourceDir + File.separator + "temp"));
		whenNew(JarFile.class).withAnyArguments().thenReturn(mockedJarFile);
		when(mockedJarFile.entries()).thenReturn(mockedEnumeration);
		when(mockedEnumeration.hasMoreElements()).thenReturn(true, false);
		when(mockedEnumeration.nextElement()).thenReturn(mockedJarEntry);
		when(mockedJarEntry.getName()).thenReturn(".sh");

		// create file to copy
		FileOutputStream fos = new FileOutputStream(resultFile);
		whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fos);

		//
		when(mockedJarFile.getInputStream(any())).thenReturn(mockedInputStream);
		when(mockedInputStream.available()).thenReturn(1, 0);
		when(mockedInputStream.read()).thenReturn(0);

		// call method
		scriptsLoader.load(sourceDir, destDir);

		// assertions
		verify(mockedJarFile, times(1)).entries();
		verify(mockedJarEntry, atLeast(1)).getName();
		verify(mockedInputStream, atLeast(1)).available();

		// clean files created
		resultFile.delete();
	}

	@Test
	public void testLoadDirAlreadyExists() throws Exception {

		//
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		when(mockedFile.exists()).thenReturn(true);

		// call method
		scriptsLoader.load(sourceDir, destDir);

		verify(mockedFile, times(0)).listFiles();
		verify(mockedFile, times(0)).getName();

	}

	@Test
	public void testLoadJarNotExist() throws Exception {

		//
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		when(mockedFile.exists()).thenReturn(false);
		File[] files = new File[1];
		files[0] = mockedFile;
		when(mockedFile.listFiles()).thenReturn(files);

		// bundle to search for not exist
		when(mockedFile.getName()).thenReturn("notExist");

		// call method
		scriptsLoader.load(sourceDir, destDir);

		verify(mockedFile, times(0)).toPath();

	}
}
