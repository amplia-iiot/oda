package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.osgi.proxies.EventAdminProxy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Collections;

public class ConfigurableBundleImpl implements ConfigurableBundle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableBundleImpl.class);

    static final String CONFIGURATION_EVENT_BASE_TOPIC = "org/osgi/service/cm/ConfigurationEvent/";
    static final String CONFIGURATION_UPDATED_EVENT = "CM_UPDATED";
    static final String CONFIGURATION_DELETED_EVENT = "CM_DELETED";
    static final String CONFIGURATION_ERROR_EVENT = "CM_ERROR";

    static final String MESSAGE_PROPERTY_NAME = "message";
    static final String CONFIGURATION_UPDATED_MESSAGE = "Configuration updated";
    static final String CONFIGURATION_DELETED_MESSAGE = "Configuration deleted";

    private final String bundleName;
    private final EventAdminProxy eventAdmin;
    private final ConfigurationUpdateHandler handler;
    private final List<ServiceRegistration<?>> serviceRegistrations;

    private final ServiceRegistration<ManagedService> configServiceRegistration;


    public ConfigurableBundleImpl(BundleContext bundleContext, ConfigurationUpdateHandler handler,
                                  List<ServiceRegistration<?>> serviceInterfacesToNotify) {
        this.bundleName = bundleContext.getBundle().getSymbolicName();
        this.eventAdmin = new EventAdminProxy(bundleContext);
        this.handler = handler;
        this.serviceRegistrations = serviceInterfacesToNotify;

        Dictionary<String, Object> managedServiceProps = new Hashtable<>();
        managedServiceProps.put(Constants.SERVICE_PID, bundleName);
        this.configServiceRegistration =
                bundleContext.registerService(ManagedService.class, this, managedServiceProps);
    }

    public ConfigurableBundleImpl(BundleContext bundleContext, ConfigurationUpdateHandler handler) {
        this(bundleContext, handler, Collections.emptyList());
    }

    @Override
    public void persistConfiguration(Dictionary<String, ?> props) {
        configServiceRegistration.setProperties(props);
    }

    @Override
    public void updated(Dictionary<String, ?> props) throws ConfigurationException {
        String configurationEvent = null;
        String configurationMessage = null;

        try {
            if (props != null) {
                configurationEvent = CONFIGURATION_UPDATED_EVENT;
                handler.loadConfiguration(props);
                configurationMessage = CONFIGURATION_UPDATED_MESSAGE;
            }
            else {
                configurationEvent = CONFIGURATION_DELETED_EVENT;
                handler.loadDefaultConfiguration();
                configurationMessage = CONFIGURATION_DELETED_MESSAGE;
            }

            handler.applyConfiguration();
        } catch (Exception exception) {
            configurationEvent = CONFIGURATION_ERROR_EVENT;
            configurationMessage = exception.toString();
            throw new ConfigurationException(bundleName, configurationMessage, exception);
        } finally {
            setServiceRegistrationsProperties(props);
            sendConfigurationEvent(configurationEvent, configurationMessage);
        }
    }

    private void setServiceRegistrationsProperties(Dictionary<String, ?> props) {
        serviceRegistrations.forEach(serviceRegistration -> serviceRegistration.setProperties(props));
    }

    private void sendConfigurationEvent(String type, String message) {
        try {
            String topic = CONFIGURATION_EVENT_BASE_TOPIC + type;
            Map<String, String> properties = new HashMap<>();
            properties.put(EventConstants.BUNDLE_SYMBOLICNAME, bundleName);
            properties.put(MESSAGE_PROPERTY_NAME, message);

            Event event = new Event(topic, properties);
            eventAdmin.postEvent(event);
        } catch (Exception exception) {
            LOGGER.warn("Configuration update event cannot be sent: {}", exception.getMessage());
        }
    }

    @Override
    public void close() {
        configServiceRegistration.unregister();
        eventAdmin.close();
    }
}
