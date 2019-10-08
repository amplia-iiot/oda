package es.amplia.oda.core.commons.diozero;


import java.util.Map;

/**
 * API to use the ADC service.
 */
public interface AdcService {
    // TODO: Make documentation for this
    /**
     * Get ADC pin associated with the given name.
     * @param channelName ADC pin name.
     * @return ADC pin associated with the given name.
     * @throws AdcDeviceException Exception getting the ADC pin with the given associated name.
     */
    AdcChannel getChannelByName(String channelName) throws AdcDeviceException;

    /**
     * Get the ADC pin associated with the given pin's terminal index.
     * @param index Index of the ADC pin's terminal.
     * @return ADC pin associated with the given pin's terminal index.
     * @throws AdcDeviceException Exception getting the ADC pin with the given pin's terminal index.
     */
    AdcChannel getChannelByIndex(int index) throws AdcDeviceException;

    /**
     * Get the available ADC pins with the associated pin's terminal index.
     * @return The available ADC pins with the associated pin's terminal index.
     */
    Map<Integer, AdcChannel> getAvailableChannels();
}
