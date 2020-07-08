package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import es.amplia.oda.datastreams.deviceinfo.configuration.ScriptsLoader;
import es.amplia.oda.datastreams.deviceinfo.datastreams.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ConfigurableBundle configurableBundle;
    private ScriptsLoader scriptsLoader;

    private ServiceRegistration<DeviceInfoProvider> deviceIdProviderRegistration;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForSerialNumber;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDeviceId;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForClock;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForUptime;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForCpuTotal;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForCpuStatus;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForCpuUsage;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForRamTotal;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForRamUsage;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDiskTotal;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDiskUsage;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForSoftware;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForTemperatureStatus;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForTemperatureValue;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Datastreams Getter Device Info");

        CommandProcessor commandProcessor = new CommandProcessorImpl();
        DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter = new DeviceInfoDatastreamsGetter(commandProcessor, bundleContext.getBundles());
        scriptsLoader = new ScriptsLoader(commandProcessor);
        ConfigurationUpdateHandler configHandler = new DeviceInfoConfigurationHandler(deviceInfoDatastreamsGetter, scriptsLoader);
        deviceIdProviderRegistration =
                bundleContext.registerService(DeviceInfoProvider.class, deviceInfoDatastreamsGetter, null);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
                Collections.singletonList(deviceIdProviderRegistration));

        datastreamsGetterRegistrationForDeviceId =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.DEVICE_ID_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getDeviceId), null);
        datastreamsGetterRegistrationForSerialNumber =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.SERIAL_NUMBER_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getSerialNumber), null);
        datastreamsGetterRegistrationForClock =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.CLOCK_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getClock), null);
        datastreamsGetterRegistrationForUptime =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.UPTIME_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getUptime), null);
        datastreamsGetterRegistrationForCpuTotal =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.CPU_TOTAL_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getCpuTotal), null);
        datastreamsGetterRegistrationForCpuStatus =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.CPU_STATUS_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getCpuStatus), null);
        datastreamsGetterRegistrationForCpuUsage =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.CPU_USAGE_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getCpuUsage), null);
        datastreamsGetterRegistrationForRamTotal =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.RAM_TOTAL_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getRamTotal), null);
        datastreamsGetterRegistrationForRamUsage =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.RAM_USAGE_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getRamUsage), null);
        datastreamsGetterRegistrationForDiskTotal =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.DISK_TOTAL_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getDiskTotal), null);
        datastreamsGetterRegistrationForDiskUsage =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.DISK_USAGE_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getDiskUsage), null);
        datastreamsGetterRegistrationForSoftware =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.SOFTWARE_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getSoftware), null);
        datastreamsGetterRegistrationForTemperatureStatus =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.TEMPERATURE_STATUS_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getTemperatureStatus), null);
        datastreamsGetterRegistrationForTemperatureValue =
                bundleContext.registerService(DatastreamsGetter.class,
                        new DatastreamGetterTemplate(DeviceInfoDatastreamsGetter.TEMPERATURE_VALUE_DATASTREAM_ID,
                                deviceInfoDatastreamsGetter::getTemperatureValue), null);

        LOGGER.info("Datastreams Getter Device Info started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Datastreams Getter Device");

        datastreamsGetterRegistrationForTemperatureValue.unregister();
        datastreamsGetterRegistrationForTemperatureStatus.unregister();
        datastreamsGetterRegistrationForSoftware.unregister();
        datastreamsGetterRegistrationForDiskUsage.unregister();
        datastreamsGetterRegistrationForDiskTotal.unregister();
        datastreamsGetterRegistrationForRamUsage.unregister();
        datastreamsGetterRegistrationForRamTotal.unregister();
        datastreamsGetterRegistrationForCpuUsage.unregister();
        datastreamsGetterRegistrationForCpuStatus.unregister();
        datastreamsGetterRegistrationForCpuTotal.unregister();
        datastreamsGetterRegistrationForUptime.unregister();
        datastreamsGetterRegistrationForClock.unregister();
        datastreamsGetterRegistrationForSerialNumber.unregister();
        datastreamsGetterRegistrationForDeviceId.unregister();
        deviceIdProviderRegistration.unregister();
        configurableBundle.close();
        try {
            scriptsLoader.close();
        } catch (Exception e) {
            LOGGER.error("Error trying to close scripts loader", e);
        }

        LOGGER.info("Datastreams Getter Device stopped");
    }
}
