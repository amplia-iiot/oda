package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.osgi.proxies.I2CServiceProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.i2c.configuration.DatastreamI2CConfigurationHandler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


	private I2CServiceProxy i2cService;
	private I2CDatastreamsRegistry i2cDatastreamsRegistry;
	private ConfigurationUpdateHandler configurationHandler;
	private ConfigurableBundleImpl configurableBundle;
	private ServiceListenerBundle<I2CService> i2cServiceListener;


	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting I2C datastreams bundle");

		i2cService = new I2CServiceProxy(bundleContext);
		I2CDatastreamsFactory factory = new I2CDatastreamsFactoryImpl(i2cService);
		ServiceRegistrationManager<DatastreamsGetter> getterRegistrationManager =
				new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
		ServiceRegistrationManager<DatastreamsSetter> setterRegistrationManager =
				new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);
		i2cDatastreamsRegistry =
				new I2CDatastreamsRegistry(factory, getterRegistrationManager, setterRegistrationManager);
		configurationHandler = new DatastreamI2CConfigurationHandler(i2cDatastreamsRegistry, i2cService);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationHandler);
		i2cServiceListener = new ServiceListenerBundle<>(bundleContext, I2CService.class, this::onServiceChanged);

		LOGGER.info("I2C datastreams bundle started");
	}

	void onServiceChanged() {
		try {
			configurationHandler.applyConfiguration();
		} catch (Exception e) {
			LOGGER.warn("Exception applying configuration: ", e);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping I2C datastreams bundle");

		i2cServiceListener.close();
		configurableBundle.close();
		i2cDatastreamsRegistry.close();
		i2cService.close();

		LOGGER.info("I2C datastreams bundle stopped");
	}
}
