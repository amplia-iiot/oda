package es.amplia.oda.datastreams.diozero;

import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.AdcServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.diozero.configuration.DatastreamsAdcConfigurationHandler;
import es.amplia.oda.event.api.EventDispatcherProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private AdcServiceProxy adcService;
	private EventDispatcherProxy eventDispatcher;
	private ConfigurableBundle configurableBundle;

	private ServiceListenerBundle<AdcService> adcServiceListener;
	private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderServiceListener;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		LOGGER.info("Starting ADC datastreams bundle");

		adcService = new AdcServiceProxy(bundleContext);
		eventDispatcher = new EventDispatcherProxy(bundleContext);
		DatastreamsRegistry registry = new DatastreamsRegistry(bundleContext, adcService, eventDispatcher);

		ConfigurationUpdateHandler configurationUpdateHandler = new DatastreamsAdcConfigurationHandler(registry, adcService);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationUpdateHandler);
		adcServiceListener = new ServiceListenerBundle<>(bundleContext, AdcService.class,
				() -> onServiceChanged(configurationUpdateHandler));
		deviceInfoProviderServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
				() -> onServiceChanged(configurationUpdateHandler));

		LOGGER.info("ADC datastreams bundle started");
	}

	void onServiceChanged(ConfigurationUpdateHandler configurationUpdateHandler) {
		try {
			configurationUpdateHandler.applyConfiguration();
		} catch (Exception exception) {
			LOGGER.warn("Exception applying configuration: ", exception);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		LOGGER.info("Stopping ADC datastreams bundle");

		deviceInfoProviderServiceListener.close();
		adcServiceListener.close();
		configurableBundle.close();
		adcService.close();
		eventDispatcher.close();

		LOGGER.info("Stopped datastreams bundle stopped");
	}
}
