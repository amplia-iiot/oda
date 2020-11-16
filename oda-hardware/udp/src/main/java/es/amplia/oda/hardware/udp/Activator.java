package es.amplia.oda.hardware.udp;

import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.udp.configuration.JavaUdpConfigurationUpdateHandler;
import es.amplia.oda.hardware.udp.udp.JavaUdpService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private JavaUdpService udpService;

	private ConfigurableBundle configurableBundle;

	private ServiceRegistration<UdpService> udpServiceRegistration;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting Java UDP south side bundle");


		udpService = new JavaUdpService();

		udpServiceRegistration =
				bundleContext.registerService(UdpService.class, udpService, null);

		JavaUdpConfigurationUpdateHandler configurationUpdateHandler = new JavaUdpConfigurationUpdateHandler(udpService);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationUpdateHandler);

		LOGGER.info("Java UDP south side bundle started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping Java UDP south side bundle");

		configurableBundle.close();
		udpServiceRegistration.unregister();
		udpService.stop();

		LOGGER.info("Java UDP south side bundle stopped");
	}
}
