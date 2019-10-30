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
     */
    AdcChannel getChannelByName(String channelName);

    /**
     * Get the ADC channel associated with the given pin's hardware index.
     * @param index Index of the ADC channel's terminal.
     * @return ADC channel associated with the given pin's hardware index.
     */
    AdcChannel getChannelByIndex(int index);

    /**
     * Get the available ADC channels with the associated pin's hardware index.
     * @return The available ADC channel with the associated pin's hardware index.
     */
    Map<Integer, AdcChannel> getAvailableChannels();
}
