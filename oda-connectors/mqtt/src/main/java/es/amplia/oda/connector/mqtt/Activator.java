package es.amplia.oda.connector.mqtt;

import es.amplia.oda.connector.mqtt.configuration.ConfigurationUpdateHandlerImpl;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleNotifierService;

import org.osgi.framework.*;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * MQTT external communications bundle activator.
 */
public class Activator implements BundleActivator {

    /**
     * Class logger
     */
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    /**
     * MQTT Connector
     */
    private MqttConnector connector;

    /**
     * Device Identifier provider service listener.
     */
    private ConfigurationUpdateHandlerImpl deviceIdProviderServiceListener;

    /**
     * Event Admin service tracker.
     */
    private ServiceTracker<EventAdmin, EventAdmin> eventAdminServiceTracker;

    /**
     * Config service registration.
     */
    private DispatcherProxy dispatcher;
    private ServiceRegistration<OpenGateConnector> openGateConnectorRegistration;

    private DeviceInfoProviderProxy deviceIdProvider;
    private ServiceRegistration<ManagedService> configServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) throws InvalidSyntaxException {
        logger.info("MQTT connector is starting");

        dispatcher = new DispatcherProxy(bundleContext);
        connector = new MqttConnector(dispatcher);
        openGateConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, connector, null);

        deviceIdProvider = new DeviceInfoProviderProxy(bundleContext);
        deviceIdProviderServiceListener = new ConfigurationUpdateHandlerImpl(connector, deviceIdProvider);
        String bundleSymbolicName = bundleContext.getBundle().getSymbolicName();
        bundleContext.addServiceListener(deviceIdProviderServiceListener, String.format("(%s=%s)", Constants.OBJECTCLASS, DeviceInfoProvider.class.getName()));
        eventAdminServiceTracker = new ServiceTracker<>(bundleContext, EventAdmin.class, null);
        eventAdminServiceTracker.open();
        ConfigurableBundleNotifierService configService =
                new ConfigurableBundleNotifierService(bundleSymbolicName, deviceIdProviderServiceListener,
                        eventAdminServiceTracker.getService());
        Dictionary<String, String> props = new Hashtable<>();
        props.put("service.pid", bundleSymbolicName);
        configServiceRegistration = bundleContext.registerService(ManagedService.class, configService, props);

        logger.info("MQTT connector started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        logger.info("MQTT connector is stopping");

        configServiceRegistration.unregister();
        eventAdminServiceTracker.close();
        deviceIdProvider.close();
        bundleContext.removeServiceListener(deviceIdProviderServiceListener);

        openGateConnectorRegistration.unregister();
        connector.close();
        dispatcher.close();

        logger.info("MQTT connector stopped");
    }
}
