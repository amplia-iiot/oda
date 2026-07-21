package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.FileSystemWatcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public class MainDirectoryWatcher implements DirectoryWatcher {

	Path path;
	RuleEngine engine;
	FileSystemWatcher watcher;

	public MainDirectoryWatcher(Path path, RuleEngine engine) {
		this.path = path;
		this.engine = engine;
	}

	@Override
	public void start() {
		watcher = new FileSystemWatcher(path, this::handleEvent, ENTRY_CREATE, ENTRY_DELETE);
		watcher.start();
	}

	private void handleEvent(Path dir, WatchEvent<?> event) {
		if (event.kind().name().equals(ENTRY_CREATE.name())) {
			engine.createDatastreamDirectory(event.context().toString());
		} else if (event.kind().name().equals(ENTRY_DELETE.name())) {
			engine.deleteDatastreamDirectory(event.context().toString());
		}
	}

	@Override
	public void stop() {
		if (watcher != null) {
			watcher.stop();
		}
	}
}
