package es.amplia.oda.datastreams.testing;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.osgi.proxies.UdpServiceProxy;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.testing.configuration.LoraDatastreamsConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private UdpServiceProxy udpService;
	private SerializerProxy serializer;
	private EventPublisherProxy eventPublisher;
	private ServiceListenerBundle<UdpService> udpServiceServiceListener;
	private ConfigurableBundle configurableBundle;
	private LoraDatastreamsConfigurationHandler configurationHandler;

	private LoraDatastreamsOrchestrator loraDatastreamsOrchestrator;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting UDP Reader datastreams bundle");

		udpService = new UdpServiceProxy(bundleContext);
		serializer = new SerializerProxy(bundleContext, ContentType.JSON);
		eventPublisher = new EventPublisherProxy(bundleContext);
		loraDatastreamsOrchestrator = new LoraDatastreamsOrchestrator(udpService, eventPublisher, serializer);

		configurationHandler = new LoraDatastreamsConfigurationHandler(loraDatastreamsOrchestrator);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationHandler);

		udpServiceServiceListener = new ServiceListenerBundle<>(bundleContext, UdpService.class,
				this::onServiceChanged);

		LOGGER.info("UDP Reader datastreams bundle started");
	}

	private void onServiceChanged() {
		try {
			LOGGER.info("Applying new configuration for LoRa datastreams");
			configurationHandler.applyConfiguration();
		} catch (Exception e) {
			LOGGER.error("Error trying to applying new configuration of LoRa datastreams.");
		}
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping UDP Reader datastreams bundle");
		udpServiceServiceListener.close();
		configurableBundle.close();
		loraDatastreamsOrchestrator.close();
		eventPublisher.close();
		serializer.close();
		udpService.close();
		LOGGER.info("UDP Reader datastreams bundle stopped");
	}
}
