package es.amplia.oda.operation.api.engine;

import es.amplia.oda.core.commons.utils.FileSystemWatcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;

public class OperationDirectoryWatcher {

	Path path;
	OperationEngine engine;
	FileSystemWatcher watcher;

	public OperationDirectoryWatcher(Path path, OperationEngine engine) {
		this.path = path;
		this.engine = engine;
	}

	public void start() {
		watcher = new FileSystemWatcher(path, this::handleEvent, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		watcher.start();
	}

	private void handleEvent(Path dir, WatchEvent<?> event) {
		// only react to javascript files; reload the whole operation set on any change
		if (event.context().toString().endsWith(".js")) {
			engine.reloadAllOperations();
		}
	}

	public void stop() {
		if (watcher != null) {
			watcher.stop();
		}
	}
}
