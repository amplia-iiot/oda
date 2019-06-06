package es.amplia.oda.core.commons.utils;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ServiceLocatorOsgi<T> implements ServiceLocator<T> {

    private final Class<T> clazz;
    private final ServiceTracker<T, T> serviceTracker;


    public ServiceLocatorOsgi(BundleContext bundleContext, Class<T> clazz) {
        this.clazz = clazz;
        serviceTracker = new ServiceTracker<>(bundleContext, clazz, null);
        serviceTracker.open();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        T[] services = serviceTracker.getServices((T[]) Array.newInstance(clazz, 0));
        return Arrays.asList(services);
    }

    @Override
    public void close() {
        serviceTracker.close();
    }
}
