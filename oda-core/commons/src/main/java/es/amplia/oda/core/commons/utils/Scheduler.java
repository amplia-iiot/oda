package es.amplia.oda.core.commons.utils;

import java.util.concurrent.TimeUnit;

public interface Scheduler extends AutoCloseable {
    void schedule(Runnable task, long initialDelay, long period, TimeUnit timeUnit);
    void clear();
    void close();
}
