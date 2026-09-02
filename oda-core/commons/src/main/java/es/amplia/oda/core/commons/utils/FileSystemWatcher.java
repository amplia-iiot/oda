package es.amplia.oda.core.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Watches a directory for filesystem events and dispatches each event to a handler.
 *
 * Replaces the per-module hand-rolled {@link WatchService} + raw {@code new Thread} loops
 * (which were duplicated across the rule engine and operations bundles) with a single
 * implementation backed by an {@link ExecutorService}. The handler receives the watched
 * directory and the raw {@link WatchEvent} so callers keep full control of the reaction
 * (event kind filtering, file extension checks, path composition, etc.).
 */
public class FileSystemWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemWatcher.class);

    /**
     * Coalesce the two separate events the OS may emit for a single modification
     * (content update + timestamp update) into one. If duplicates still slip through,
     * this window may need to be larger.
     */
    static final long EVENT_DEBOUNCE_MILLIS = 50;

    private final Path path;
    private final BiConsumer<Path, WatchEvent<?>> eventHandler;
    private final WatchEvent.Kind<?>[] kinds;

    private WatchService watchService;
    private ExecutorService executor;

    public FileSystemWatcher(Path path, BiConsumer<Path, WatchEvent<?>> eventHandler, WatchEvent.Kind<?>... kinds) {
        this.path = path;
        this.eventHandler = eventHandler;
        this.kinds = kinds;
    }

    public void start() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, kinds);
            executor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "oda-fs-watcher-" + path.getFileName());
                // daemon so a watcher never keeps the JVM alive on shutdown
                thread.setDaemon(true);
                return thread;
            });
            executor.submit(this::watch);
        } catch (IOException e) {
            LOGGER.error("Error creating filesystem watcher for directory {}", path, e);
        }
    }

    private void watch() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                Thread.sleep(EVENT_DEBOUNCE_MILLIS);
                key.pollEvents().forEach(event -> eventHandler.accept(path, event));
                key.reset();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Filesystem watcher for directory {} was interrupted. Watcher will be stopped", path, e);
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.warn("Error closing filesystem watcher for directory {}", path, e);
            }
        }
    }
}
