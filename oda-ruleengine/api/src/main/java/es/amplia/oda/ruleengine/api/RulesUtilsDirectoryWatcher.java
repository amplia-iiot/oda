package es.amplia.oda.ruleengine.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.*;

public class RulesUtilsDirectoryWatcher implements DirectoryWatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesUtilsDirectoryWatcher.class);

	Path path;
	RuleEngine engine;
	WatchService creatingWatcher;

	Thread creatingWatcherThread;


	public RulesUtilsDirectoryWatcher(Path path, RuleEngine engine) {
		this.path = path;
		this.engine = engine;
	}

	@Override
	public void start() {
		try {
			creatingWatcher = FileSystems.getDefault().newWatchService();
			path.register(creatingWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			creatingWatcherThread = new Thread(() -> {
				try {
					while (true) {
						WatchKey key = creatingWatcher.take();

						// Prevent receiving two separate ENTRY_MODIFY events: file modified
						// and timestamp updated. Instead, receive one ENTRY_MODIFY event
						// with two counts.
						// If it still happens, sleep time might have to be bigger
						Thread.sleep( 50 );

						key.pollEvents().forEach(event -> {
							// only check files ending with .js (javascript files)
							// in linux, when a file is modified, a temporal file called goutputstream is created
							// with this condition we avoid the watcher to detect this temporary file
							if (event.context().toString().endsWith(".js")) {
								// reload all rules
								engine.reloadAllRules();
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
			LOGGER.error("Error on Rules utils directory Watcher creation", e);
		}
	}

	@Override
	public void stop() {
		creatingWatcherThread.interrupt();
	}
}
