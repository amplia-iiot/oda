package es.amplia.oda.core.commons.utils;

public interface ServiceRegistrationManagerWithKey<K,S> {
    void register(K key, S service);
    void unregister(K key);
    void unregisterAll();
}
