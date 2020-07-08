package es.amplia.oda.ruleengine.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public class RulesDirectoryWatcher implements DirectoryWatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesDirectoryWatcher.class);

	Path path;
	RuleEngine engine;
	WatchService creatingWatcher;

	Thread creatingWatcherThread;


	public RulesDirectoryWatcher(Path path, RuleEngine engine) {
		this.path = path;
		this.engine = engine;
	}

	@Override
	public void start() {
		try {
			creatingWatcher = FileSystems.getDefault().newWatchService();
			path.register(creatingWatcher, ENTRY_CREATE, ENTRY_DELETE);
			creatingWatcherThread = new Thread(() -> {
				try {
					while(true) {
						WatchKey key = creatingWatcher.take();
						key.pollEvents().forEach(event -> {
							if (event.kind().name().equals(ENTRY_CREATE.name())) {
								engine.createRule(this.path.toString() + FileSystems.getDefault().getSeparator() + event.context().toString());
							} else if (event.kind().name().equals(ENTRY_DELETE.name())) {
								engine.deleteRule(this.path.toString() + FileSystems.getDefault().getSeparator() + event.context().toString());
							}
						});
						key.reset();
					}
				} catch (InterruptedException e) {
					LOGGER.error("Something unexpected happened while watching the directory for changes. Watcher will be stopped", e);
					Thread.currentThread().interrupt();
				}
			});
			creatingWatcherThread.start();
		} catch (IOException e) {
			LOGGER.error("Error on Rules directory Watcher creation", e);
		}
	}

	@Override
	public void stop() {
		creatingWatcherThread.interrupt();
	}
}
