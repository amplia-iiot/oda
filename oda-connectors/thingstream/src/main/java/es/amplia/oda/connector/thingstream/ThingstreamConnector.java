package es.amplia.oda.connector.thingstream;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration;

import com.myriadgroup.iot.sdk.IoTSDKConstants;
import com.myriadgroup.iot.sdk.client.message.IMessageClient;
import com.myriadgroup.iot.sdk.client.message.IMessageReceivedCallback;
import com.myriadgroup.iot.sdk.client.message.MessageClientException;
import com.myriadgroup.iot.sdk.client.protocol.layer.ProtocolLayerException;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.QOS;

public class ThingstreamConnector implements OpenGateConnector, IMessageReceivedCallback, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ThingstreamConnector.class);

    static final int MESSAGE_LIMIT = 80;

    private final ATManager atManager;
    private final Dispatcher dispatcher;

    private String dataTopic;
    private String operationTopic;
    private IMessageClient.QOS qos;
    private IMessageClient messageClient;

    ThingstreamConnector(ATManager atManager, Dispatcher dispatcher) {
        this.atManager = atManager;
        this.dispatcher = dispatcher;
    }

    public void loadConfigurationAndInit(ConnectorConfiguration configuration) throws MessageClientException {
        close();

        dataTopic = configuration.getDataTopic();
        operationTopic = configuration.getOperationTopic();
        qos = parseQos(configuration.getQos());
        messageClient = MessageClientFactory.createMessageClient(configuration, atManager);

        messageClient.create();
        messageClient.connect(true, this);
        messageClient.subscribe(dataTopic, qos);
        if (!operationTopic.isEmpty()) {
            messageClient.subscribe(operationTopic, qos);
        }
    }

    private IMessageClient.QOS parseQos(QOS qos) {
        switch (qos) {
            case QOS_0:
                return IMessageClient.QOS.QOS0;
            case QOS_1:
                return IMessageClient.QOS.QOS1;
            case QOS_2:
                return IMessageClient.QOS.QOS2;
            default:
                throw new IllegalArgumentException("Unknown Quality of Service");
        }
    }

    @Override
    public void uplink(byte[] payload) {
        try {
            String message = new String(payload, IoTSDKConstants.DEFAULT_ENCODING);
            logger.info("Send message through thingstream connector: {}", message);

            if (messageClient == null || !messageClient.isConnected()) {
                logger.error("Error sending message through thingstream connector: Thingstream client is not connected");
            } else if (message.length() > MESSAGE_LIMIT) {
                logger.error("Error sending message through thingstream connector: Message is too long");
            } else {
                messageClient.publish(dataTopic, qos, message.getBytes(IoTSDKConstants.DEFAULT_ENCODING));
            }
        } catch (MessageClientException | UnsupportedEncodingException e) {
            logger.error("Error sending message through thingstream connector: {}", e.getMessage());
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return messageClient != null && messageClient.isConnected();
        } catch (MessageClientException e) {
            return false;
        }
    }

    @Override
    public void onReceivedServerDisconnect() {
        logger.warn("server disconnect");
    }

    @Override
    public void onReceiveError(ProtocolLayerException ex) {
        logger.error("handle error: {}", ex);
    }

    @Value
    private static class Message {
        private byte[] data;

        @Override
        public String toString() {
            return new String(data);
        }
    }

    @Override
    public void onReceive(String topicName, byte[] data, int qos) {
        Message message = new Message(data);
        logger.info("received messages - dataTopic: {}. msg: {}, qos: {}", topicName, message, qos);
        if (topicName.equals(operationTopic)) {
            dispatcher.process(message.getData());
        }
    }

    @Override
    public void close() {
        if (messageClient != null) {
            try {
                messageClient.unsubscribe(dataTopic);
                if (operationTopic != null && !operationTopic.isEmpty()) {
                    messageClient.unsubscribe(operationTopic);
                }
                messageClient.disconnect();
                messageClient.destroy();
            } catch (MessageClientException e) {
                logger.error("Error closing Thingstream connector: {}", e.getMessage());
            }
        }
    }
}
