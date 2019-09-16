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

	private ScriptsLoader scriptsLoader;
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
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForApn;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForClock;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForUptime;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForTemperatureValue;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForTemperatureStatus;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForCpuStatus;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForCpuUsage;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForCpuTotal;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForRamUsage;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForRamTotal;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDiskUsage;
	private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDiskTotal;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting Datastreams Getter Device Info");

		CommandProcessor commandProcessor = new CommandProcessorImpl();
		scriptsLoader = new ScriptsLoaderImpl(commandProcessor);
		DeviceInfoFX30 deviceInfoFX30 = new DeviceInfoFX30(commandProcessor, bundleContext.getBundles());
		ConfigurationUpdateHandler configHandler = new DeviceInfoFX30ConfigurationHandler(scriptsLoader, deviceInfoFX30);
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
		datastreamsGetterRegistrationForApn =
				bundleContext.registerService(DatastreamsGetter.class,
						new ApnDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForClock =
				bundleContext.registerService(DatastreamsGetter.class,
						new ClockDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForUptime =
				bundleContext.registerService(DatastreamsGetter.class,
						new UptimeDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForTemperatureValue =
				bundleContext.registerService(DatastreamsGetter.class,
						new TemperatureValueDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForTemperatureStatus =
				bundleContext.registerService(DatastreamsGetter.class,
						new TemperatureStatusDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForCpuStatus =
				bundleContext.registerService(DatastreamsGetter.class,
						new CpuStatusDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForCpuUsage =
				bundleContext.registerService(DatastreamsGetter.class,
						new CpuUsageDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForCpuTotal =
				bundleContext.registerService(DatastreamsGetter.class,
						new CpuTotalDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForRamUsage =
				bundleContext.registerService(DatastreamsGetter.class,
						new RamUsageDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForRamTotal =
				bundleContext.registerService(DatastreamsGetter.class,
						new RamTotalDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForDiskUsage =
				bundleContext.registerService(DatastreamsGetter.class,
						new DiskUsageDatastreamGetter(deviceInfoFX30), null);
		datastreamsGetterRegistrationForDiskTotal =
				bundleContext.registerService(DatastreamsGetter.class,
						new DiskTotalDatastreamGetter(deviceInfoFX30), null);

		LOGGER.info("Datastreams Getter Device Info started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping Datastreams Getter Device");

		datastreamsGetterRegistrationForDiskTotal.unregister();
		datastreamsGetterRegistrationForDiskUsage.unregister();
		datastreamsGetterRegistrationForRamTotal.unregister();
		datastreamsGetterRegistrationForRamUsage.unregister();
		datastreamsGetterRegistrationForCpuTotal.unregister();
		datastreamsGetterRegistrationForCpuUsage.unregister();
		datastreamsGetterRegistrationForCpuStatus.unregister();
		datastreamsGetterRegistrationForTemperatureStatus.unregister();
		datastreamsGetterRegistrationForTemperatureValue.unregister();
		datastreamsGetterRegistrationForUptime.unregister();
		datastreamsGetterRegistrationForClock.unregister();
		datastreamsGetterRegistrationForSerialNumber.unregister();
		datastreamsGetterRegistrationForDeviceId.unregister();
		datastreamsGetterRegistrationForMaker.unregister();
		datastreamsGetterRegistrationForModel.unregister();
		datastreamsGetterRegistrationForImei.unregister();
		datastreamsGetterRegistrationForImsi.unregister();
		datastreamsGetterRegistrationForIcc.unregister();
		datastreamsGetterRegistrationForRssi.unregister();
		datastreamsGetterRegistrationForSoftware.unregister();
		datastreamsGetterRegistrationForApn.unregister();
		deviceIdProviderRegistration.unregister();
		configurableBundle.close();
		scriptsLoader.close();

		LOGGER.info("Datastreams Getter Device stopped");
	}
}
