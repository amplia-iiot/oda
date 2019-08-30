package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;

import org.osgi.framework.BundleContext;

import java.util.List;

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
    public int getCpuUsage() { return proxy.callFirst(DeviceInfoProvider::getCpuUsage); }
    @Override
    public String getCpuStatus() { return proxy.callFirst(DeviceInfoProvider::getCpuStatus); }
    @Override
    public int getCpuTotal() { return proxy.callFirst(DeviceInfoProvider::getCpuTotal); }
    @Override
    public int getRamUsage() { return proxy.callFirst(DeviceInfoProvider::getRamUsage); }
    @Override
    public long getRamTotal() { return proxy.callFirst(DeviceInfoProvider::getRamTotal); }
    @Override
    public int getDiskUsage() { return proxy.callFirst(DeviceInfoProvider::getDiskUsage); }
    @Override
    public long getDiskTotal() { return proxy.callFirst(DeviceInfoProvider::getDiskTotal); }
    @Override
    public String getClock() { return proxy.callFirst(DeviceInfoProvider::getClock); }
    @Override
    public long getUptime() { return proxy.callFirst(DeviceInfoProvider::getUptime); }
    @Override
    public List<Software> getSoftware() { return proxy.callFirst(DeviceInfoProvider::getSoftware); }
    @Override
    public String getTemperatureStatus() { return proxy.callFirst(DeviceInfoProvider::getTemperatureStatus); }
    @Override
    public int getTemperatureValue() { return proxy.callFirst(DeviceInfoProvider::getTemperatureValue); }

    @Override
    public void close() {
        proxy.close();
    }
}
