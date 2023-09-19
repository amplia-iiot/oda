package es.amplia.oda.ruleengine.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RulesDirectoryWatcher.class)
public class RulesDirectoryWatcherTest {

	@Mock
	RuleEngine mockedEngine;
	@Mock
	Path mockedPath;

	@InjectMocks
	RulesDirectoryWatcher testDirectoryWatcher;

	@Mock
	Thread mockedThread;

	@Test
	public void testConstructor() {
		String path = "this/is/a/path";

		testDirectoryWatcher = new RulesDirectoryWatcher(Paths.get(path), mockedEngine);

		assertEquals(Paths.get(path), Whitebox.getInternalState(testDirectoryWatcher, "path"));
		assertEquals(mockedEngine, Whitebox.getInternalState(testDirectoryWatcher, "engine"));
	}

	@Test
	public void testStart() throws Exception {
		Whitebox.setInternalState(testDirectoryWatcher, "creatingWatcherThread", mockedThread);
		whenNew(Thread.class).withAnyArguments().thenReturn(mockedThread);

		testDirectoryWatcher.start();

		verify(mockedPath).register(any(WatchService.class), eq(StandardWatchEventKinds.ENTRY_CREATE),
				eq(StandardWatchEventKinds.ENTRY_DELETE), eq(StandardWatchEventKinds.ENTRY_MODIFY));
		verify(mockedThread).start();
	}

	@Test
	public void testThreadCreateEvent() throws InterruptedException, IOException {
		String root = new File(".").getCanonicalPath();
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new RulesDirectoryWatcher(Paths.get(testRoute), mockedEngine);

		testDirectoryWatcher.start();
		File fileToCreate = new File(testRoute + "/tempDir.js");
		fileToCreate.createNewFile();

		TimeUnit.MILLISECONDS.sleep(500);
		verify(mockedEngine).createRule(testRoute + "/tempDir.js");

		fileToCreate.delete();
	}

	@Test
	public void testThreadDeleteEvent() throws InterruptedException, IOException {
		String root = new File(".").getCanonicalPath();
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new RulesDirectoryWatcher(Paths.get(testRoute), mockedEngine);
		File fileToCreate = new File(testRoute + "/tempDir.js");
		fileToCreate.createNewFile();

		testDirectoryWatcher.start();
		fileToCreate.delete();

		TimeUnit.MILLISECONDS.sleep(500);
		verify(mockedEngine).deleteRule(testRoute + "/tempDir.js");
	}

	@Test
	public void testThreadException() throws IOException {
		String root = new File(".").getCanonicalPath();
		String testRoute = root + "/src/test/java";
		testDirectoryWatcher = new RulesDirectoryWatcher(Paths.get(testRoute), mockedEngine);

		testDirectoryWatcher.start();
		testDirectoryWatcher.stop();

		verify(mockedEngine, never()).createDatastreamDirectory("tempDir");
		verify(mockedEngine, never()).deleteDatastreamDirectory("tempDir");
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testDirectoryWatcher, "creatingWatcherThread", mockedThread);

		testDirectoryWatcher.stop();

		verify(mockedThread).interrupt();
	}
}
