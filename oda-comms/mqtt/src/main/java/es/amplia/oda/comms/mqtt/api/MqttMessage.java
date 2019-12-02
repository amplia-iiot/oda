package es.amplia.oda.comms.mqtt.api;

import java.util.Arrays;
import java.util.Objects;

public class MqttMessage {

    public static final int DEFAULT_QOS = 1;
    public static final boolean DEFAULT_RETAINED = false;

    private final byte[] payload;
    private final int qos;
    private final boolean retained;

    private MqttMessage(byte[] payload, int qos, boolean retained) {
        this.payload = payload.clone();
        this.qos = qos;
        this.retained = retained;
    }

    public static MqttMessage newInstance(byte[] payload) {
        return new MqttMessage(payload, DEFAULT_QOS, DEFAULT_RETAINED);
    }

    public static MqttMessage newInstance(byte[] payload, int qos, boolean retained) {
        return new MqttMessage(payload, qos, retained);
    }

    public byte[] getPayload() {
        return payload.clone();
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetained() {
        return retained;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MqttMessage message = (MqttMessage) o;
        return qos == message.qos &&
                retained == message.retained &&
                Arrays.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(qos, retained);
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }

    @Override
    public String toString() {
        return "MqttMessage{" +
                "payload=" + new String(payload) +
                ", qos=" + qos +
                ", retained=" + retained +
                '}';
    }
}
