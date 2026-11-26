package es.amplia.oda.hardware.diozero;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.configuration.DioZeroConfigurationHandler;
import es.amplia.oda.hardware.diozero.gpio.DioZeroGpioService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


	private DioZeroAdcService adcService;
	private DioZeroGpioService gpioService;
	private ConfigurableBundle configurableBundle;
	private ServiceRegistration<AdcService> adcServiceRegistration;
	private ServiceRegistration<GpioService> gpioServiceRegistration;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting Device I/O Zero bundle");

		adcService = new DioZeroAdcService();
		gpioService = new DioZeroGpioService();
		DioZeroConfigurationHandler configHandler = new DioZeroConfigurationHandler(adcService, gpioService);
		adcServiceRegistration = bundleContext.registerService(AdcService.class, adcService, null);
		gpioServiceRegistration = bundleContext.registerService(GpioService.class, gpioService, null);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
				Arrays.asList(adcServiceRegistration, gpioServiceRegistration));

		LOGGER.info("Device I/O Zero bundle started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping Device I/O Zero bundle");

		adcServiceRegistration.unregister();
		gpioServiceRegistration.unregister();
		configurableBundle.close();
		adcService.close();
		gpioService.close();

		LOGGER.info("Device I/O Zero bundle stopped");
	}
}
