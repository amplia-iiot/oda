package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;

import org.osgi.framework.BundleContext;

public class DeviceInfoProviderProxy implements DeviceInfoProvider, AutoCloseable {

    private final OsgiServiceProxy<DeviceInfoProvider> proxy;

    public DeviceInfoProviderProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(DeviceInfoProvider.class, bundleContext);
    }

    @Override
    public String getDeviceId() { return proxy.callFirst(DeviceInfoProvider::getDeviceId); }

    @Override
    public String getApiKey() { return proxy.callFirst(DeviceInfoProvider::getApiKey); }

    @Override
    public void close() {
        proxy.close();
    }
}
