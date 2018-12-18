package es.amplia.oda.core.commons.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

public class ServiceRegistrationManagerOsgi<S> implements ServiceRegistrationManager<S> {

    private final BundleContext bundleContext;
    private final Class<S> serviceClass;

    private final List<ServiceRegistration<S>> registrations = new ArrayList<>();

    public ServiceRegistrationManagerOsgi(BundleContext bundleContext, Class<S> serviceClass) {
        this.bundleContext = bundleContext;
        this.serviceClass = serviceClass;
    }

    @Override
    public void register(S service) {
        registrations.add(bundleContext.registerService(serviceClass, service, null));
    }

    @Override
    public void unregister() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }
}
