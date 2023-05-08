package es.amplia.oda.demo.scadatables.info.configuration;

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

    /**
     * Data type.
     */
    private final String dataType;
    /**
     * Datastream identifier.
     */
    private final String datastreamId;
    /**
     * Initial value for entry
     */
    private String defaultValue;
    /**
     * Transformation script for recollected value
     */
    private Invocable script;
}
