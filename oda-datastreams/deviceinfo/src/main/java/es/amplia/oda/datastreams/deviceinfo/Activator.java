package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.ConfigurableBundleNotifierService;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {
    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    /**
     * Datastreams Getter service registration.
     */
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForSerialNumber;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDeviceId;

    /**
     * Device id provider service registration.
     */
    private ServiceRegistration<DeviceInfoProvider> deviceIdProviderRegistration;

    /**
     * Event Admin service tracker.
     */
    private ServiceTracker<EventAdmin, EventAdmin> eventAdminServiceTracker;

    /**
     * Configuration service registration.
     */
    private ServiceRegistration<ManagedService> configServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        logger.info("Starting Datastreams Getter Device Info");

        DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter = new DeviceInfoDatastreamsGetter();
        this.datastreamsGetterRegistrationForDeviceId     = bundleContext.registerService(DatastreamsGetter.class,  deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId(),     null);
        this.datastreamsGetterRegistrationForSerialNumber = bundleContext.registerService(DatastreamsGetter.class,  deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber(), null);
        deviceIdProviderRegistration                      = bundleContext.registerService(DeviceInfoProvider.class, deviceInfoDatastreamsGetter, null);

        String bundleName = bundleContext.getBundle().getSymbolicName();
        ConfigurationUpdateHandler configHandler = new DeviceInfoConfigurationHandler(deviceInfoDatastreamsGetter);
        eventAdminServiceTracker = new ServiceTracker<>(bundleContext, EventAdmin.class, null);
        eventAdminServiceTracker.open();
        ConfigurableBundleNotifierService configService =
                new ConfigurableBundleNotifierService(bundleName, configHandler, eventAdminServiceTracker.getService(),
                        Collections.singletonList(deviceIdProviderRegistration));
        Dictionary<String, String> configServiceProps = new Hashtable<>();
        configServiceProps.put(Constants.SERVICE_PID, bundleName);
        configServiceRegistration = bundleContext.registerService(ManagedService.class, configService, configServiceProps);

        logger.info("Datastreams Getter Device Info started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        logger.info("Stopping Datastreams Getter Device");

        configServiceRegistration.unregister();
        eventAdminServiceTracker.close();

        datastreamsGetterRegistrationForDeviceId.unregister();
        datastreamsGetterRegistrationForSerialNumber.unregister();
        deviceIdProviderRegistration.unregister();

        logger.info("Datastreams Getter Device stopped");
    }
}
