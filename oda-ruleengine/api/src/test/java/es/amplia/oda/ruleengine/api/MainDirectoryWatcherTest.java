package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.FileSystemWatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MainDirectoryWatcherTest {

	@Mock
	RuleEngine mockedEngine;
	@Mock
	Path mockedPath;

	@InjectMocks
	MainDirectoryWatcher testDirectoryWatcher;

	@Test
	public void testConstructor() {
		String path = "this/is/a/path";

		testDirectoryWatcher = new MainDirectoryWatcher(Paths.get(path), mockedEngine);

		assertEquals(Paths.get(path), Whitebox.getInternalState(testDirectoryWatcher, "path"));
		assertEquals(mockedEngine, Whitebox.getInternalState(testDirectoryWatcher, "engine"));
	}

	@Test
	public void testStart() {
		try (MockedConstruction<FileSystemWatcher> watcherConstruction = mockConstruction(FileSystemWatcher.class)) {
			testDirectoryWatcher.start();

			assertEquals(1, watcherConstruction.constructed().size());
			verify(watcherConstruction.constructed().get(0)).start();
		}
	}

	@Test
	public void testThreadCreateEvent() throws IOException {
		String root = new File(".").getCanonicalPath();
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new MainDirectoryWatcher(Paths.get(testRoute), mockedEngine);

		testDirectoryWatcher.start();
		File fileToCreate = new File(testRoute + "/tempDir");
		fileToCreate.createNewFile();

		Mockito.verify(mockedEngine, Mockito.timeout(1000).atLeastOnce()).createDatastreamDirectory("tempDir");

		fileToCreate.delete();
		testDirectoryWatcher.stop();
	}

	@Test
	public void testThreadDeleteEvent() throws IOException {
		String root = new File(".").getCanonicalPath();
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new MainDirectoryWatcher(Paths.get(testRoute), mockedEngine);
		File fileToCreate = new File(testRoute + "/tempDir");
		fileToCreate.createNewFile();

		testDirectoryWatcher.start();
		fileToCreate.delete();

		Mockito.verify(mockedEngine, Mockito.timeout(1000).atLeastOnce()).deleteDatastreamDirectory("tempDir");
		testDirectoryWatcher.stop();
	}

	@Test
	public void testThreadException() {
		String root;
		try {
			root = new File(".").getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new MainDirectoryWatcher(Paths.get(testRoute), mockedEngine);

		testDirectoryWatcher.start();
		testDirectoryWatcher.stop();

		verify(mockedEngine, never()).createDatastreamDirectory("tempDir");
		verify(mockedEngine, never()).deleteDatastreamDirectory("tempDir");
	}

	@Test
	public void testStop() {
		FileSystemWatcher mockedWatcher = mock(FileSystemWatcher.class);
		Whitebox.setInternalState(testDirectoryWatcher, "watcher", mockedWatcher);

		testDirectoryWatcher.stop();

		verify(mockedWatcher).stop();
	}
}
