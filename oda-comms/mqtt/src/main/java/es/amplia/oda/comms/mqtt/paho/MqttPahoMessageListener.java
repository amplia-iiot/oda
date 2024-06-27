package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

@Slf4j
class MqttPahoMessageListener implements IMqttMessageListener {

    private static final int NUM_THREADS = 10;
    private static final int MAX_SIZE_THREADS_QUEUE = 1000;

    private final MqttMessageListener mqttMessageListener;
    private final IMqttAsyncClient innerMqttClient;
    private final ExecutorService executor;

    MqttPahoMessageListener(MqttMessageListener mqttMessageListener, IMqttAsyncClient mqttClient) {
        this.mqttMessageListener = mqttMessageListener;
        this.innerMqttClient = mqttClient;

        this.executor = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS,
            0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(MAX_SIZE_THREADS_QUEUE));
    }

    public MqttMessageListener getMqttMessageListener() {
        return mqttMessageListener;
    }

    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
        // send ACK message received
        try {
            innerMqttClient.messageArrivedComplete(message.getId(), 1);
        } catch (MqttException e) {
            log.error("Error sending message received ACK. Message id = {}",message.getId());
        }

        try {
            executor.execute(() -> // process message arrived
                    mqttMessageListener.messageArrived(topic,
                            MqttMessage.newInstance(message.getPayload(), message.getQos(), message.isRetained())));
            log.debug("Thread pool queue - pending tasks = {}, remaining capacity = {}",
                    ((ThreadPoolExecutor) executor).getQueue().size(),
                    ((ThreadPoolExecutor) executor).getQueue().remainingCapacity());
        } catch (RejectedExecutionException e) {
            log.error("Can't add task to thread pool, reached max size", e);
        }
    }
}
