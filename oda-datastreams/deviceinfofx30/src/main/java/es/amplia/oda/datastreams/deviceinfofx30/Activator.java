package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30ConfigurationHandler;
import es.amplia.oda.datastreams.deviceinfofx30.datastreams.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ConfigurableBundle configurableBundle;

	private ServiceRegistration<DeviceInfoProvider> deviceIdProviderRegistration;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForSerialNumber;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDeviceId;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForMaker;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForModel;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForImei;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForImsi;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForIcc;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForRssi;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForSoftware;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForIpPresence;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForIpAddress;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForApn;


	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting Datastreams Getter Device Info");

		CommandProcessor commandProcessor = new CommandProcessorImpl();
		DeviceInfoFX30 deviceInfoFX30 = new DeviceInfoFX30(commandProcessor, bundleContext.getBundle());
		ConfigurationUpdateHandler configHandler = new DeviceInfoFX30ConfigurationHandler(deviceInfoFX30);
		deviceIdProviderRegistration =
				bundleContext.registerService(DeviceInfoProvider.class, deviceInfoFX30, null);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
				Collections.singletonList(deviceIdProviderRegistration));
		datastreamsGetterRegistrationForSerialNumber =
				bundleContext.registerService(DatastreamsGetter.class,
						new SerialNumberDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForDeviceId =
				bundleContext.registerService(DatastreamsGetter.class,
						new DeviceIdDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForMaker =
				bundleContext.registerService(DatastreamsGetter.class,
						new MakerDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForModel =
				bundleContext.registerService(DatastreamsGetter.class,
						new ModelDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForImei =
				bundleContext.registerService(DatastreamsGetter.class,
						new ImeiDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForImsi =
				bundleContext.registerService(DatastreamsGetter.class,
						new ImsiDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForIcc =
				bundleContext.registerService(DatastreamsGetter.class,
						new IccDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForRssi =
				bundleContext.registerService(DatastreamsGetter.class,
						new RssiDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForSoftware =
				bundleContext.registerService(DatastreamsGetter.class,
						new SoftwareDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForIpPresence =
				bundleContext.registerService(DatastreamsGetter.class,
						new IpPresenceDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForIpAddress =
				bundleContext.registerService(DatastreamsGetter.class,
						new IpAddressDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForApn =
				bundleContext.registerService(DatastreamsGetter.class,
						new ApnDatastreamGetter(deviceInfoFX30), null);

		LOGGER.info("Datastreams Getter Device Info started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping Datastreams Getter Device");

		datastreamsGetterRegistrationForSerialNumber.unregister();
		datastreamsGetterRegistrationForDeviceId.unregister();
		datastreamsGetterRegistrationForMaker.unregister();
		datastreamsGetterRegistrationForModel.unregister();
		datastreamsGetterRegistrationForImei.unregister();
		datastreamsGetterRegistrationForImsi.unregister();
		datastreamsGetterRegistrationForIcc.unregister();
		datastreamsGetterRegistrationForRssi.unregister();
		datastreamsGetterRegistrationForSoftware.unregister();
		datastreamsGetterRegistrationForIpPresence.unregister();
		datastreamsGetterRegistrationForIpAddress.unregister();
		datastreamsGetterRegistrationForApn.unregister();
		deviceIdProviderRegistration.unregister();
		configurableBundle.close();

		LOGGER.info("Datastreams Getter Device stopped");
	}
}
