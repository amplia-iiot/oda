package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.nio.charset.StandardCharsets;

/**
 * Last Will and Testament configuration.
 */
public class LwtConfiguration implements MqttConfiguration {

    /**
     * LWT topic.
     */
    private final String topic;

    /**
     * LWT payload.
     */
    private final byte[] payload;

    /**
     * LWT QoS.
     */
    private final int qualityOfService;

    /**
     * LWT retained policy.
     */
    private final boolean retained;

    /**
     * Constructor.
     *
     * @param topic            LWT topic.
     * @param payload          LWT payload.
     * @param qualityOfService LWT QoS.
     * @param retained         LWT retained policy.
     */
    LwtConfiguration(String topic, byte[] payload, int qualityOfService, boolean retained) {
        this.topic = topic;
        this.payload = payload;
        this.qualityOfService = qualityOfService;
        this.retained = retained;
    }

    /**
     * Constructor.
     *
     * @param topic            LWT topic.
     * @param payload          LWT payload as String.
     * @param qualityOfService LWT QoS.
     * @param retained         LWT retained policy.
     */
    LwtConfiguration(String topic, String payload, int qualityOfService, boolean retained) {
        this(topic, payload.getBytes(StandardCharsets.UTF_8), qualityOfService, retained);
    }

    /**
     * Configure the Mqtt Connect Options with the LWT configuration.
     *
     * @param options options to configure the LWT.
     */
    public void configure(MqttConnectOptions options) {
        options.setWill(topic, payload, qualityOfService, retained);
    }
}
