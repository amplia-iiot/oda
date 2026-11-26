package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.FileSystemWatcher;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;

public class RulesUtilsDirectoryWatcher implements DirectoryWatcher {

	Path path;
	RuleEngine engine;
	FileSystemWatcher watcher;

	public RulesUtilsDirectoryWatcher(Path path, RuleEngine engine) {
		this.path = path;
		this.engine = engine;
	}

	@Override
	public void start() {
		watcher = new FileSystemWatcher(path, this::handleEvent, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		watcher.start();
	}

	private void handleEvent(Path dir, WatchEvent<?> event) {
		// only react to javascript files; reload the whole rule set on any change
		if (event.context().toString().endsWith(".js")) {
			engine.reloadAllRules();
		}
	}

	@Override
	public void stop() {
		if (watcher != null) {
			watcher.stop();
		}
	}
}
