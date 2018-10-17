package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Represent a MQTT configuration.
 */
public interface MqttConfiguration {
    /**
     * Configure the given Mqtt connect options with the current configuration.
     *
     * @param options MQTT connect options to configure.
     */
    void configure(MqttConnectOptions options);
}
