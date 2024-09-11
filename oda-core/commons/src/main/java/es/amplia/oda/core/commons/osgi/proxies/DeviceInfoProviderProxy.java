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
    public Integer getCpuUsage() { return proxy.callFirst(DeviceInfoProvider::getCpuUsage); }
    @Override
    public String getCpuStatus() { return proxy.callFirst(DeviceInfoProvider::getCpuStatus); }
    @Override
    public Integer getCpuTotal() { return proxy.callFirst(DeviceInfoProvider::getCpuTotal); }
    @Override
    public Integer getRamUsage() { return proxy.callFirst(DeviceInfoProvider::getRamUsage); }
    @Override
    public Long getRamTotal() { return proxy.callFirst(DeviceInfoProvider::getRamTotal); }
    @Override
    public Integer getDiskUsage() { return proxy.callFirst(DeviceInfoProvider::getDiskUsage); }
    @Override
    public Long getDiskTotal() { return proxy.callFirst(DeviceInfoProvider::getDiskTotal); }
    @Override
    public String getClock() { return proxy.callFirst(DeviceInfoProvider::getClock); }
    @Override
    public Long getUptime() { return proxy.callFirst(DeviceInfoProvider::getUptime); }
    @Override
    public List<Software> getSoftware() { return proxy.callFirst(DeviceInfoProvider::getSoftware); }
    @Override
    public String getTemperatureStatus() { return proxy.callFirst(DeviceInfoProvider::getTemperatureStatus); }
    @Override
    public Integer getTemperatureValue() { return proxy.callFirst(DeviceInfoProvider::getTemperatureValue); }

    @Override
    public void close() {
        proxy.close();
    }
}
