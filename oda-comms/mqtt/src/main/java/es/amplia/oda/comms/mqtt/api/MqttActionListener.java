package es.amplia.oda.comms.mqtt.api;

public interface MqttActionListener {
    void onSuccess();
	
    void onFailure(Throwable exception);
}
