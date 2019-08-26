package es.amplia.oda.dispatcher.opengate;

interface Scheduler extends AutoCloseable{
    void schedule(Runnable task, long initialDelay, long period);
    void clear();
    void close();
}
