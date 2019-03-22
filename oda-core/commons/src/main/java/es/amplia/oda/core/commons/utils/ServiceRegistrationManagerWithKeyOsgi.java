package es.amplia.oda.core.commons.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistrationManagerWithKeyOsgi<K, S> implements ServiceRegistrationManagerWithKey<K, S> {

    private final BundleContext bundleContext;
    private final Class<S> serviceClass;

    private final Map<K, ServiceRegistration<S>> registrations = new HashMap<>();

    public ServiceRegistrationManagerWithKeyOsgi(BundleContext bundleContext, Class<S> serviceClass) {
        this.bundleContext = bundleContext;
        this.serviceClass = serviceClass;
    }

    @Override
    public void register(K key, S service) {
        registrations.put(key, bundleContext.registerService(serviceClass, service, null));
    }

    @Override
    public void unregister(K key) {
        if (registrations.containsKey(key)) {
            registrations.get(key).unregister();
            registrations.remove(key);
        }
    }

    @Override
    public void unregisterAll() {
        registrations.values().forEach(ServiceRegistration::unregister);
        registrations.clear();
    }
}
