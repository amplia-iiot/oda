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
						new DatastreamGetterTemplate(DeviceInfoFX30.SERIAL_NUMBER_DATASTREAM_ID,
								deviceInfoFX30::getSerialNumber), null);
		datastreamsGetterRegistrationForDeviceId =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.DEVICE_ID_DATASTREAM_ID,
								deviceInfoFX30::getDeviceId), null);
		datastreamsGetterRegistrationForMaker =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.MAKER_DATASTREAM_ID,
								()->"Sierra Wireless"), null);
		datastreamsGetterRegistrationForModel =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.MODEL_DATASTREAM_ID,
								deviceInfoFX30::getModel), null);
		datastreamsGetterRegistrationForImei =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.IMEI_DATASTREAM_ID,
								deviceInfoFX30::getImei), null);
		datastreamsGetterRegistrationForImsi =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.IMSI_DATASTREAM_ID,
								deviceInfoFX30::getImsi), null);
		datastreamsGetterRegistrationForIcc =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.ICC_DATASTREAM_ID,
								deviceInfoFX30::getIcc), null);
		datastreamsGetterRegistrationForRssi =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.RSSI_DATASTREAM_ID,
								deviceInfoFX30::getRssi), null);
		datastreamsGetterRegistrationForSoftware =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.SOFTWARE_DATASTREAM_ID,
								deviceInfoFX30::getSoftware), null);
		datastreamsGetterRegistrationForApn =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.APN_DATASTREAM_ID,
								deviceInfoFX30::getApn), null);
		datastreamsGetterRegistrationForClock =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.CLOCK_DATASTREAM_ID,
								deviceInfoFX30::getClock), null);
		datastreamsGetterRegistrationForUptime =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.UPTIME_DATASTREAM_ID,
								deviceInfoFX30::getUptime), null);
		datastreamsGetterRegistrationForTemperatureValue =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.TEMPERATURE_VALUE_DATASTREAM_ID,
								deviceInfoFX30::getTemperatureValue), null);
		datastreamsGetterRegistrationForTemperatureStatus =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.TEMPERATURE_STATUS_DATASTREAM_ID,
								deviceInfoFX30::getTemperatureStatus), null);
		datastreamsGetterRegistrationForCpuStatus =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.CPU_STATUS_DATASTREAM_ID,
								deviceInfoFX30::getCpuStatus), null);
		datastreamsGetterRegistrationForCpuUsage =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.CPU_USAGE_DATASTREAM_ID,
								deviceInfoFX30::getCpuUsage), null);
		datastreamsGetterRegistrationForCpuTotal =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.CPU_TOTAL_DATASTREAM_ID,
								deviceInfoFX30::getCpuTotal), null);
		datastreamsGetterRegistrationForRamUsage =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.RAM_USAGE_DATASTREAM_ID,
								deviceInfoFX30::getRamUsage), null);
		datastreamsGetterRegistrationForRamTotal =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.RAM_TOTAL_DATASTREAM_ID,
								deviceInfoFX30::getRamTotal), null);
		datastreamsGetterRegistrationForDiskUsage =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.DISK_USAGE_DATASTREAM_ID,
								deviceInfoFX30::getDiskUsage), null);
		datastreamsGetterRegistrationForDiskTotal =
				bundleContext.registerService(DatastreamsGetter.class,
						new DatastreamGetterTemplate(DeviceInfoFX30.DISK_TOTAL_DATASTREAM_ID,
								deviceInfoFX30::getDiskTotal), null);

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
