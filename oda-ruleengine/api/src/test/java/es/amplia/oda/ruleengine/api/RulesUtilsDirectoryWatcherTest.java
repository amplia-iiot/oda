package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.FileSystemWatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RulesUtilsDirectoryWatcherTest {

	@Mock
	RuleEngine mockedEngine;
	@Mock
	Path mockedPath;

	@InjectMocks
	RulesUtilsDirectoryWatcher testDirectoryWatcher;

	@Test
	public void testConstructor() {
		String path = "this/is/a/path";

		testDirectoryWatcher = new RulesUtilsDirectoryWatcher(Paths.get(path), mockedEngine);

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
		testDirectoryWatcher = new RulesUtilsDirectoryWatcher(Paths.get(testRoute), mockedEngine);

		testDirectoryWatcher.start();

		File fileToCreate = new File(testRoute + "/tempDir.js");

		// erase file if it already exists
		fileToCreate.delete();

		// create file
		fileToCreate.createNewFile();

		Mockito.verify(mockedEngine, Mockito.timeout(1000).atLeastOnce()).reloadAllRules();

		fileToCreate.delete();
		testDirectoryWatcher.stop();
	}

	@Test
	public void testThreadDeleteEvent() throws IOException {
		String root = new File(".").getCanonicalPath();
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new RulesUtilsDirectoryWatcher(Paths.get(testRoute), mockedEngine);
		File fileToCreate = new File(testRoute + "/tempDir.js");
		fileToCreate.createNewFile();

		testDirectoryWatcher.start();
		fileToCreate.delete();

		Mockito.verify(mockedEngine, Mockito.timeout(1000).atLeastOnce()).reloadAllRules();
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
		testDirectoryWatcher = new RulesUtilsDirectoryWatcher(Paths.get(testRoute), mockedEngine);

		testDirectoryWatcher.start();
		testDirectoryWatcher.stop();

		verify(mockedEngine, never()).reloadAllRules();
	}

	@Test
	public void testStop() {
		FileSystemWatcher mockedWatcher = mock(FileSystemWatcher.class);
		Whitebox.setInternalState(testDirectoryWatcher, "watcher", mockedWatcher);

		testDirectoryWatcher.stop();

		verify(mockedWatcher).stop();
	}
}
