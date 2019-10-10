package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.AdcServiceProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.adc.configuration.DatastreamsAdcConfigurationHandler;
import es.amplia.oda.datastreams.adc.datastreams.DatastreamsFactoryImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


	private AdcServiceProxy adcService;
	private EventDispatcherProxy eventDispatcher;
	private ConfigurationUpdateHandler configurationUpdateHandler;
	private ConfigurableBundle configurableBundle;
	private ServiceListenerBundle<AdcService> adcServiceListener;
	private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderServiceListener;


	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting ADC datastreams bundle");

		adcService = new AdcServiceProxy(bundleContext);
		eventDispatcher = new EventDispatcherProxy(bundleContext);
		DatastreamsFactory datastreamsFactory = new DatastreamsFactoryImpl(adcService, eventDispatcher);
		ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
				new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
		DatastreamsRegistry registry = new DatastreamsRegistry(datastreamsFactory, datastreamsGetterRegistrationManager);
		configurationUpdateHandler = new DatastreamsAdcConfigurationHandler(registry, adcService);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationUpdateHandler);
		adcServiceListener = new ServiceListenerBundle<>(bundleContext, AdcService.class, this::onServiceChanged);
		deviceInfoProviderServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
				this::onServiceChanged);

		LOGGER.info("ADC datastreams bundle started");
	}

	void onServiceChanged() {
		try {
			configurationUpdateHandler.applyConfiguration();
		} catch (Exception exception) {
			LOGGER.warn("Exception applying configuration: ", exception);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping ADC datastreams bundle");

		deviceInfoProviderServiceListener.close();
		adcServiceListener.close();
		configurableBundle.close();
		configurationUpdateHandler = null;
		adcService.close();
		eventDispatcher.close();

		LOGGER.info("Stopped datastreams bundle stopped");
	}
}
