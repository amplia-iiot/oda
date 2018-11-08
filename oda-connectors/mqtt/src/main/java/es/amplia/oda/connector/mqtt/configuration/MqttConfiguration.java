package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

interface MqttConfiguration {
    void configure(MqttConnectOptions options);
}
