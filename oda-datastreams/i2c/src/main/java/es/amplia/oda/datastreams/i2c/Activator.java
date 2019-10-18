package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.I2CServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.i2c.configuration.DatastreamI2CConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private I2CServiceProxy i2cService;
	private ConfigurableBundleImpl configurableBundle;
	private ServiceListenerBundle<I2CService> i2cServiceListener;
	private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderServiceListener;
	private I2CDatastreamsRegistry i2cDatastreamsRegistry;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting I2C datastreams bundle");

		i2cService = new I2CServiceProxy(bundleContext);
		i2cDatastreamsRegistry = new I2CDatastreamsRegistry(bundleContext, i2cService);

		ConfigurationUpdateHandler configurationHandler = new DatastreamI2CConfigurationHandler(i2cDatastreamsRegistry, i2cService);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationHandler);
		i2cServiceListener = new ServiceListenerBundle<>(bundleContext, I2CService.class,
				() -> onServiceChanged(configurationHandler));
		deviceInfoProviderServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
				() -> onServiceChanged(configurationHandler));

		LOGGER.info("I2C datastreams bundle started");
	}

	void onServiceChanged(ConfigurationUpdateHandler configurationHandler) {
		try {
			configurationHandler.applyConfiguration();
		} catch (Exception e) {
			LOGGER.warn("Exception applying configuration: ", e);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping I2C datastreams bundle");

		deviceInfoProviderServiceListener.close();
		i2cDatastreamsRegistry.close();
		i2cServiceListener.close();
		configurableBundle.close();
		i2cService.close();

		LOGGER.info("I2C datastreams bundle stopped");
	}
}
