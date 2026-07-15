package es.amplia.oda.datastreams.deviceinfo.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ScriptsLoaderTest {

	@InjectMocks
	private ScriptsLoader scriptsLoader;
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

		try (MockedConstruction<File> fileCons = mockConstruction(File.class,
				(mock, mctx) -> {
					when(mock.exists()).thenReturn(false);
					when(mock.listFiles()).thenReturn(new File[] { mock });
					when(mock.getName()).thenReturn("es.amplia.oda.datastreams.deviceinfo");
					when(mock.toPath()).thenReturn(Paths.get(sourceDir + File.separator + "temp"));
				});
			 MockedConstruction<JarFile> jarFileCons = mockConstruction(JarFile.class,
				(mock, mctx) -> {
					when(mock.entries()).thenReturn(mockedEnumeration);
					when(mock.getInputStream(any())).thenReturn(mockedInputStream);
				});
			 MockedConstruction<FileOutputStream> fosCons = mockConstruction(FileOutputStream.class)) {

			//
			when(mockedEnumeration.hasMoreElements()).thenReturn(true, false);
			when(mockedEnumeration.nextElement()).thenReturn(mockedJarEntry);
			when(mockedJarEntry.getName()).thenReturn(".sh");

			//
			when(mockedInputStream.available()).thenReturn(1, 0);
			when(mockedInputStream.read()).thenReturn(0);

			// call method
			scriptsLoader.load(sourceDir, destDir);

			// assertions
			verify(jarFileCons.constructed().get(0), times(1)).entries();
			verify(mockedJarEntry, atLeast(1)).getName();
			verify(mockedInputStream, atLeast(1)).available();
		}

		// clean files created
		resultFile.delete();
	}

	@Test
	public void testLoadDirAlreadyExists() throws Exception {

		try (MockedConstruction<File> fileCons = mockConstruction(File.class,
				(mock, mctx) -> when(mock.exists()).thenReturn(true))) {

			// call method
			scriptsLoader.load(sourceDir, destDir);

			verify(fileCons.constructed().get(0), times(0)).listFiles();
			verify(fileCons.constructed().get(0), times(0)).getName();
		}
	}

	@Test
	public void testLoadJarNotExist() throws Exception {

		try (MockedConstruction<File> fileCons = mockConstruction(File.class,
				(mock, mctx) -> {
					when(mock.exists()).thenReturn(false);
					when(mock.listFiles()).thenReturn(new File[] { mock });
					// bundle to search for not exist
					when(mock.getName()).thenReturn("notExist");
				})) {

			// call method
			scriptsLoader.load(sourceDir, destDir);

			for (File constructedFile : fileCons.constructed()) {
				verify(constructedFile, times(0)).toPath();
			}
		}
	}
}
