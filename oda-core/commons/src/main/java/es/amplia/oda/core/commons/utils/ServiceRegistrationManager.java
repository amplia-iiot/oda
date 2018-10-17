package es.amplia.oda.core.commons.utils;

public interface ServiceRegistrationManager<S> {
    void register(S service);
    void unregister();
}
