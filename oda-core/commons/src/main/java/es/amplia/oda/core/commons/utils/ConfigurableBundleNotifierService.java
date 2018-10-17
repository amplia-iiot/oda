package es.amplia.oda.core.commons.utils;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.*;

/**
 * Class implementing the OSGi ManagedService to allow the bundle configuration
 * and responsible to send an OSGi event notifying the result of the
 * configuration update.
 */
public class ConfigurableBundleNotifierService implements ManagedService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableBundleNotifierService.class);

    static final String CONFIGURATION_EVENT_BASE_TOPIC = "org/osgi/service/cm/ConfigurationEvent/";
    static final String CONFIGURATION_UPDATED_EVENT = "CM_UPDATED";
    static final String CONFIGURATION_DELETED_EVENT = "CM_DELETED";
    static final String CONFIGURATION_ERROR_EVENT = "CM_ERROR";

    static final String MESSAGE_PROPERTY_NAME = "message";
    static final String CONFIGURATION_UPDATED_MESSAGE = "Configuration updated";
    static final String CONFIGURATION_DELETED_MESSAGE = "Configuration deleted";

    private String bundleName;

    private ConfigurationUpdateHandler handler;

    private EventAdmin eventAdmin;

    private List<ServiceRegistration<?>> serviceRegistrations;

    /**
     * Constructor.
     * @param bundleName Configurable bundle symbolic name.
     * @param handler Configuration update handler.
     * @param eventAdmin Event admin.
     * @param serviceRegistrations Service Registrations.
     */
    public ConfigurableBundleNotifierService(String bundleName, ConfigurationUpdateHandler handler,
                                             EventAdmin eventAdmin, List<ServiceRegistration<?>> serviceRegistrations) {
        this.bundleName = bundleName;
        this.handler = handler;
        this.eventAdmin = eventAdmin;
        this.serviceRegistrations = serviceRegistrations;
    }

    /**
     * Constructor.
     * @param bundleName Configurable bundle symbolic name.
     * @param handler Configuration update handler.
     * @param eventAdmin Event admin.
     */
    public ConfigurableBundleNotifierService(String bundleName, ConfigurationUpdateHandler handler,
                                             EventAdmin eventAdmin) {
        this(bundleName, handler, eventAdmin, Collections.emptyList());
    }



    /**
     * Update the configuration of the bundle.
     * @param props Configuration properties.
     * @throws ConfigurationException Exception during the configuration of the bundle.
     */
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

    /**
     * Set service registrations properties to notify to service listeners.
     * @param props Properties to set.
     */
    private void setServiceRegistrationsProperties(Dictionary<String, ?> props) {
        serviceRegistrations.forEach(serviceRegistration -> serviceRegistration.setProperties(props));
    }

    /**
     * Notify the configuration result.
     * @param type Configuration type.
     * @param message Configuration result message.
     */
    private void sendConfigurationEvent(String type, String message) {
        try {
            String topic = CONFIGURATION_EVENT_BASE_TOPIC + type;
            Map<String, String> properties = new HashMap<>();
            properties.put(EventConstants.BUNDLE_SYMBOLICNAME, bundleName);
            properties.put(MESSAGE_PROPERTY_NAME, message);

            Event event = new Event(topic, properties);
            eventAdmin.postEvent(event);
        } catch (Exception exception) {
            logger.warn("Configuration update event cannot be sent: {}", exception.getMessage());
        }
    }
}
