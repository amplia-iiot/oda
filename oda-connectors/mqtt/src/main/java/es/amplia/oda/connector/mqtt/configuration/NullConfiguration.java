package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Class implementing the Null object pattern to handle optional configurations.
 */
public class NullConfiguration implements MqttConfiguration {

    /**
     * Do nothing.
     *
     * @param options MQTT connect options to configure.
     */
    @Override
    public void configure(MqttConnectOptions options) {
        // Null configuration. Nothing to do
    }
}
