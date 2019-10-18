package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.i2c.configuration.DioZeroI2CConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private DioZeroI2CService i2cService;
	private ServiceRegistration<I2CService> i2cServiceRegistration;
	private ConfigurableBundle configurableBundle;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting I2C I/O Devices bundle");

		i2cService = new DioZeroI2CService();
		DioZeroI2CConfigurationHandler configHandler = new DioZeroI2CConfigurationHandler(i2cService);
		i2cServiceRegistration = bundleContext.registerService(I2CService.class, i2cService, null);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
				Collections.singletonList(i2cServiceRegistration));

		LOGGER.info("I2C I/O Devices bundle started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping I2C I/O Devices bundle");

		configurableBundle.close();
		i2cServiceRegistration.unregister();
		i2cService.close();

		LOGGER.info("I2C I/O Devices bundle stopped");
	}
}
