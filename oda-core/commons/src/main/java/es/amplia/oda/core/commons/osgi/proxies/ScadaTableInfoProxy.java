package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;

import org.osgi.framework.BundleContext;

public class ScadaTableInfoProxy implements ScadaTableInfo, AutoCloseable {

    private final OsgiServiceProxy<ScadaTableInfo> proxy;

	public ScadaTableInfoProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(ScadaTableInfo.class, bundleContext);
	}

    @Override
    public int getNumBinaryInputs() {
        return proxy.callFirst(ScadaTableInfo::getNumBinaryInputs);
    }

    @Override
    public int getNumDoubleBinaryInputs() {
        return proxy.callFirst(ScadaTableInfo::getNumDoubleBinaryInputs);
    }

    @Override
    public int getNumAnalogInputs() {
        return proxy.callFirst(ScadaTableInfo::getNumAnalogInputs);
    }

    @Override
    public int getNumCounters() {
        return proxy.callFirst(ScadaTableInfo::getNumCounters);
    }

    @Override
    public int getNumFrozenCounters() {
        return proxy.callFirst(ScadaTableInfo::getNumFrozenCounters);
    }

    @Override
    public int getNumBinaryOutputs() {
        return proxy.callFirst(ScadaTableInfo::getNumBinaryOutputs);
    }

    @Override
    public int getNumAnalogOutputs() {
        return proxy.callFirst(ScadaTableInfo::getNumAnalogOutputs);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
