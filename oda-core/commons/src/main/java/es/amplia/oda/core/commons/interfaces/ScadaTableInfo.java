package es.amplia.oda.core.commons.interfaces;

/**
 * SCADA tables info.
 */
public interface ScadaTableInfo {
    /**
     * Get the number of binary input variables.
     * @return Number of binary inputs.
     */
    int getNumBinaryInputs();
    /**
     * Get the number of double binary input variables.
     * @return Number of double binary inputs.
     */
    int getNumDoubleBinaryInputs();
    /**
     * Get the number of analog input variables.
     * @return Number of analog inputs.
     */
    int getNumAnalogInputs();
    /**
     * Get the number of counter variables.
     * @return Number of counters.
     */
    int getNumCounters();
    /**
     * Get the number of frozen counter variables.
     * @return Number of frozen counters.
     */
    int getNumFrozenCounters();
    /**
     * Get the number of binary output variables.
     * @return Number of binary outputs.
     */
    int getNumBinaryOutputs();
    /**
     * Get the number of analog output variables.
     * @return Number of analog outputs.
     */
    int getNumAnalogOutputs();
}
