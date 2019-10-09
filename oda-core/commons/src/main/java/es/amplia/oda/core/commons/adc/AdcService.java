package es.amplia.oda.core.commons.adc;


import java.util.Map;

/**
 * ADC service API.
 */
public interface AdcService {
    /**
     * Get ADC channel associated with the given name.
     * @param channelName ADC pin channel.
     * @return ADC channel associated with the given name.
     * @throws AdcDeviceException Exception getting the ADC channel with the given associated name.
     */
    AdcChannel getChannelByName(String channelName);

    /**
     * Get the ADC channel associated with the given pin's terminal index.
     * @param index Index of the ADC channel's terminal.
     * @return ADC pin associated with the given channel's terminal index.
     * @throws AdcDeviceException Exception getting the ADC channel with the given pin's terminal index.
     */
    AdcChannel getChannelByIndex(int index);

    /**
     * Get the available ADC channels with the associated pin's terminal index.
     * @return The available ADC channel with the associated pin's terminal index.
     */
    Map<Integer, AdcChannel> getAvailableChannels();
}
