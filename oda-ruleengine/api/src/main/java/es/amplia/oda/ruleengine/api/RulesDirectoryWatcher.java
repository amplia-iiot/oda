package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.FileSystemWatcher;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;

public class RulesDirectoryWatcher implements DirectoryWatcher {

	Path path;
	RuleEngine engine;
	FileSystemWatcher watcher;

	public RulesDirectoryWatcher(Path path, RuleEngine engine) {
		this.path = path;
		this.engine = engine;
	}

	@Override
	public void start() {
		watcher = new FileSystemWatcher(path, this::handleEvent, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		watcher.start();
	}

	private void handleEvent(Path dir, WatchEvent<?> event) {
		// only check files ending with .js (javascript files); in linux, when a file is modified
		// a temporary goutputstream file is created, this filter avoids reacting to it
		if (event.context().toString().endsWith(".js")) {
			String file = dir.toString() + FileSystems.getDefault().getSeparator() + event.context().toString();
			if (event.kind().name().equals(ENTRY_CREATE.name())) {
				engine.createRule(file);
			} else if (event.kind().name().equals(ENTRY_DELETE.name())) {
				engine.deleteRule(file);
			} else if (event.kind().name().equals(ENTRY_MODIFY.name())) {
				engine.modifyRule(file);
			}
		}
	}

	@Override
	public void stop() {
		if (watcher != null) {
			watcher.stop();
		}
	}
}
