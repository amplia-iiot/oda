package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CService;
import org.osgi.framework.BundleContext;

import java.util.List;

public class I2CServiceProxy implements I2CService, AutoCloseable {

	private final OsgiServiceProxy<I2CService> proxy;

	public I2CServiceProxy(BundleContext bundleContext) {
		this.proxy = new OsgiServiceProxy<>(I2CService.class, bundleContext);
	}

	@Override
	public I2CDevice getI2CFromAddress(int controller, int address) {
		return proxy.callFirst(i2CService -> i2CService.getI2CFromAddress(controller, address));
	}

	@Override
	public I2CDevice getI2CFromName(String name) {
		return proxy.callFirst(i2CService -> i2CService.getI2CFromName(name));
	}

	@Override
	public List<I2CDevice> getAllI2CFromController(int controller) {
		return proxy.callFirst(i2CService -> i2CService.getAllI2CFromController(controller));
	}

	@Override
	public List<I2CDevice> getAllI2C() {
		return proxy.callFirst(I2CService::getAllI2C);
	}

	@Override
	public void close() {
		proxy.close();
	}
}
