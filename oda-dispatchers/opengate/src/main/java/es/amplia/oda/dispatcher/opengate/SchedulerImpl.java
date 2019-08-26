package es.amplia.oda.dispatcher.opengate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class SchedulerImpl implements Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerImpl.class);

    static final long STOP_PENDING_OPERATIONS_TIMEOUT = 10;
    static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;


    private final ScheduledExecutorService executor;
    private final List<ScheduledFuture> tasks = new ArrayList<>();


    SchedulerImpl(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void schedule(Runnable task, long initialDelay, long period) {
        tasks.add(executor.scheduleAtFixedRate(task, initialDelay, period, TIME_UNIT));
    }

    @Override
    public void clear() {
        tasks.forEach(task -> task.cancel(false));
        tasks.clear();
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(STOP_PENDING_OPERATIONS_TIMEOUT, TIME_UNIT);
        } catch (InterruptedException e) {
            LOGGER.error("The shutdown of the pool of threads took longer than {} seconds: {}",
                    STOP_PENDING_OPERATIONS_TIMEOUT, e);
            Thread.currentThread().interrupt();
        }
    }
}
