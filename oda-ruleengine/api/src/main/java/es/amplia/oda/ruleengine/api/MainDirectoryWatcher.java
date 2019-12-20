package es.amplia.oda.ruleengine.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

public class MainDirectoryWatcher implements DirectoryWatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainDirectoryWatcher.class);

	Path path;
	RuleEngine engine;
	WatchService creatingWatcher;

	Thread creatingWatcherThread;


	public MainDirectoryWatcher(Path path, RuleEngine engine) {
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
						Thread.sleep(50);
						key.pollEvents().forEach(event -> {
							if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
								engine.createDatastreamDirectory(event.context().toString());
							} else if (event.kind().name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
								engine.deleteDatastreamDirectory(event.context().toString());
							}
						});
						key.reset();
					}
				} catch (InterruptedException e) {
					LOGGER.error("Something unexpected happened during the watch of delete files");
				}
			});
			creatingWatcherThread.start();
		} catch (IOException e) {
			LOGGER.error("Error on Rules directory Watcher creation");
		}
	}

	@Override
	public void stop() {
		creatingWatcherThread.interrupt();
	}
}
