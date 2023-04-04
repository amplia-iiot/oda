package es.amplia.oda.core.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SchedulerImpl implements Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerImpl.class);
    static final long STOP_PENDING_OPERATIONS_TIMEOUT = 10;
    protected static final TimeUnit STOP_PENDING_OPERATIONS_TIME_UNIT = TimeUnit.SECONDS;


    private final ScheduledExecutorService executorService;
    private final List<ScheduledFuture> tasks = new ArrayList<>();


    public SchedulerImpl(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void schedule(Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        final Runnable taskWithExceptionCatching = () -> {
            try {
                task.run();
            } catch ( Throwable t ) {  // Catch Throwable rather than Exception (a subclass).
                LOGGER.error("Caught exception in ScheduledExecutorService. StackTrace: ", t);
            }
        };

        if (period == 0) {
            tasks.add(executorService.schedule(taskWithExceptionCatching, initialDelay, timeUnit));
        } else {
            tasks.add(executorService.scheduleAtFixedRate(taskWithExceptionCatching, initialDelay, period, timeUnit));
        }
    }

    @Override
    public void clear() {
        tasks.forEach(task -> task.cancel(false));
        tasks.clear();
    }

    @Override
    public void close() {
        clear();
        stopPendingOperations();
    }

    private void stopPendingOperations() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(STOP_PENDING_OPERATIONS_TIMEOUT, STOP_PENDING_OPERATIONS_TIME_UNIT);
        } catch (InterruptedException e) {
            LOGGER.error("The shutdown of the pool of threads its taking more than {} seconds. Will not wait longer.",
                    STOP_PENDING_OPERATIONS_TIMEOUT);
            Thread.currentThread().interrupt();
        }
    }
}
