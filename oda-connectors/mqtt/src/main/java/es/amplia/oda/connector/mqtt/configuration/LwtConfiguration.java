package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.nio.charset.StandardCharsets;

public class LwtConfiguration implements MqttConfiguration {

    private final String topic;
    private final byte[] payload;
    private final int qualityOfService;
    private final boolean retained;

    LwtConfiguration(String topic, byte[] payload, int qualityOfService, boolean retained) {
        this.topic = topic;
        this.payload = payload;
        this.qualityOfService = qualityOfService;
        this.retained = retained;
    }

    LwtConfiguration(String topic, String payload, int qualityOfService, boolean retained) {
        this(topic, payload.getBytes(StandardCharsets.UTF_8), qualityOfService, retained);
    }

    public void configure(MqttConnectOptions options) {
        options.setWill(topic, payload, qualityOfService, retained);
    }
}
