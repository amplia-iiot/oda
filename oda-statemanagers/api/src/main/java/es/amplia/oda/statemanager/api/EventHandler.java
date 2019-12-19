package es.amplia.oda.statemanager.api;

public interface EventHandler extends AutoCloseable {
    void registerStateManager(StateManager stateManager);
    void unregisterStateManager();
    @Override
    void close();
}
