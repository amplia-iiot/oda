package es.amplia.oda.comms.mqtt.api;

public class MqttException extends RuntimeException {
    public MqttException(String message) {
        super(message);
    }

    public MqttException(String message, Throwable cause) {
        super(message, cause);
    }
}
