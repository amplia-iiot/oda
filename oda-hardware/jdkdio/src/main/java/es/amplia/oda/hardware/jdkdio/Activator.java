package es.amplia.oda.hardware.jdkdio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.jdkdio.configuration.JDkDioConfigurationHandler;
import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private JdkDioGpioService gpioService;

    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<GpioService> gpioServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting JDK Device I/O bundle");

        gpioService = new JdkDioGpioService();

        JDkDioConfigurationHandler configHandler = new JDkDioConfigurationHandler(gpioService);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        gpioServiceRegistration = bundleContext.registerService(GpioService.class, gpioService, null);

        LOGGER.info("JDK Device I/O bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping JDK Device I/O bundle");

        gpioServiceRegistration.unregister();
        configurableBundle.close();
        gpioService.release();

        LOGGER.info("JDK Device I/O bundle stopped");
    }
}
