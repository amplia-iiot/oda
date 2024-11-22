package es.amplia.oda.service.scadatables.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import lombok.Data;

import javax.script.Invocable;

/**
 * SCADA variable configuration information.
 */
@SuppressWarnings("restriction")
@Data
public class ScadaTableEntryConfiguration {

    public static final String BINARY_INPUT_TYPE_NAME = "BinaryInput";
    public static final String DOUBLE_BINARY_INPUT_TYPE_NAME = "DoubleBinaryInput";
    public static final String ANALOG_INPUT_TYPE_NAME = "AnalogInput";
    public static final String COUNTER_TYPE_NAME = "Counter";
    public static final String FROZEN_COUNTER_TYPE_NAME = "FrozenCounter";
    public static final String BINARY_OUTPUT_TYPE_NAME = "BinaryOutput";
    public static final String ANALOG_OUTPUT_TYPE_NAME = "AnalogOutput";

    public static final String EVENT_PUBLISH_DISPATCHER = "dispatcher";
    public static final String EVENT_PUBLISH_STATEMANAGER = "statemanager";

    /**
     * Data type.
     */
    private String dataType;

    /**
     * Datastream identifier.
     */
    private String datastreamId;

    /**
     * Device identifier.
     */
    private String deviceId;

    /**
     * Feed identifier.
     */
    private String feed;

    /**
     * Initial value for entry
     */
    private String defaultValue;

    /**
     * Transformation script for recollected value
     */
    private Invocable script;

    /**
     * Indicates if it is an event or a response to interrogation command
     */
    private boolean isEvent;

    /**
     * Indicates the way to publish the signal in the case it is an event
     * dispatcher means that the event will be published as soon as it is received (can't apply rules)
     * statemanager means that the event will pass to the stateManager and will be published by the collector (rules can be applied)
     */
    private String eventPublish;


    public ScadaTableEntryConfiguration(String dataType, String datastreamId, String deviceId, String feed,
                                        String defaultValue, Invocable script, String eventPublish) {

        // check eventPublish is one of the allowed types
        isEventPublishValid(eventPublish);

        this.dataType = dataType;
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
        this.feed = feed;
        this.defaultValue = defaultValue;
        this.script = script;
        this.isEvent = isEvent(eventPublish);
        this.eventPublish = eventPublish;
    }

    private boolean isEvent(String eventPublish) {
        if (eventPublish == null) {
            return false;
        } else if (eventPublish.equalsIgnoreCase(EVENT_PUBLISH_DISPATCHER) || eventPublish.equalsIgnoreCase(EVENT_PUBLISH_STATEMANAGER)) {
            return true;
        }
        return false;
    }

    private void isEventPublishValid(String eventPublish) throws ConfigurationException {
        if (eventPublish != null && !eventPublish.equalsIgnoreCase(EVENT_PUBLISH_DISPATCHER) &&
                !eventPublish.equalsIgnoreCase(EVENT_PUBLISH_STATEMANAGER)) {
            throw new ConfigurationException("Event publish type " + eventPublish + " not valid");
        }
    }
}
