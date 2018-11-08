package es.amplia.oda.core.commons.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ServiceRegistrationManagerOsgi<S> implements ServiceRegistrationManager<S> {

    private final BundleContext bundleContext;
    private final Class<S> serviceClass;

    private ServiceRegistration<S> registration;

    public ServiceRegistrationManagerOsgi(BundleContext bundleContext, Class<S> serviceClass) {
        this.bundleContext = bundleContext;
        this.serviceClass = serviceClass;
    }

    @Override
    public void register(S service) {
        registration = bundleContext.registerService(serviceClass, service, null);
    }

    @Override
    public void unregister() {
        registration.unregister();
    }
}
