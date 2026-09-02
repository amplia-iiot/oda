package es.amplia.oda.core.commons.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemWatcherTest {

	private Path tempDir;
	private FileSystemWatcher watcher;

	@BeforeEach
	public void setUp() throws IOException {
		tempDir = Files.createTempDirectory("fswatcher-test");
	}

	@AfterEach
	public void tearDown() {
		if (watcher != null) {
			watcher.stop();
		}
	}

	@Test
	@Timeout(value = 5000, unit = TimeUnit.MILLISECONDS)
	public void firesHandlerOnFileEvent() throws Exception {
		List<String> events = new CopyOnWriteArrayList<>();
		watcher = new FileSystemWatcher(tempDir,
				(dir, event) -> events.add(event.kind().name() + ":" + event.context()),
				ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		watcher.start();

		File created = new File(tempDir.toFile(), "created.txt");
		assertTrue(created.createNewFile());

		long deadline = System.currentTimeMillis() + 4000;
		while (events.isEmpty() && System.currentTimeMillis() < deadline) {
			Thread.sleep(50);
		}

		assertFalse(events.isEmpty(), "expected a filesystem event to be dispatched");
		assertTrue(events.stream().anyMatch(e -> e.contains("created.txt")));
	}

	@Test
	public void stopBeforeStartIsNoOp() {
		watcher = new FileSystemWatcher(tempDir, (dir, event) -> { }, ENTRY_CREATE);
		// watchService/executor are still null; stop() must not throw
		watcher.stop();
	}
}
