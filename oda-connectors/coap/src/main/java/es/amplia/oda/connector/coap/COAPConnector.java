package es.amplia.oda.connector.coap;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.connector.coap.configuration.ConnectorConfiguration;

import lombok.Value;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public class COAPConnector implements OpenGateConnector, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(COAPConnector.class);

    static final Integer UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE = 55;
    private static final Map<ContentType, Integer> CONTENT_TYPE_MAPPER = new EnumMap<>(ContentType.class);
    static {
        CONTENT_TYPE_MAPPER.put(ContentType.CBOR, MediaTypeRegistry.APPLICATION_CBOR);
        CONTENT_TYPE_MAPPER.put(ContentType.JSON, MediaTypeRegistry.APPLICATION_JSON);
        CONTENT_TYPE_MAPPER.put(ContentType.MESSAGE_PACK, UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE);
    }


    private final COAPClientFactory coapClientFactory;
    private CoapClient client;
    private OptionSet optionSet;


    COAPConnector(COAPClientFactory coapClientFactory) {
        this.coapClientFactory = coapClientFactory;
    }

    public void loadAndInit(ConnectorConfiguration configuration) {
        close();

        client = coapClientFactory.createClient(configuration);
        optionSet = coapClientFactory.createOptions(configuration);

        LOGGER.info("Connected to {} (timeout: {} seconds)", client.getURI(), client.getTimeout());
    }

    @Value
    private static class Message {
        private byte[] payload;

        @Override
        public String toString() {
            return new String(payload);
        }
    }

    @Override
    public void uplink(byte[] payload, ContentType contentType) {
        if (client == null) {
            logErrorMessage("COAP client is not configured");
            return;
        }

        Message message = new Message(payload);
        LOGGER.info("Sending message through COAP connector");
        LOGGER.debug("Message sending through COAP connector: {}", message);

        OptionSet messageOptions = getMessageOptions(contentType);
        Request request = (Request) Request.newPost().setPayload(message.getPayload()).setOptions(messageOptions);

        try {
            CoapResponse response = client.advanced(request);

            if (response == null) {
                logErrorMessage("No response");
            } else if (!CoAP.ResponseCode.isSuccess(response.getCode())) {
                logErrorMessage(response.getCode().toString());
            } else {
                LOGGER.info("Message sent through COAP connector");
            }
        } catch (Exception e) {
            LOGGER.error("Exception sending message", e);
        }
    }

    @Override
    public void uplinkResponse(byte[] payload, ContentType arg1) {
        logErrorMessage("Send response not suported");
    }

    private OptionSet getMessageOptions(ContentType contentType) {
        return optionSet.setContentFormat(CONTENT_TYPE_MAPPER.get(contentType));
    }

    private void logErrorMessage(String errorMessage) {
        LOGGER.error("Error sending message through COAP connector: {}", errorMessage);
    }

    @Override
    public boolean isConnected() {
        return client != null && client.ping();
    }

    @Override
    public void close() {
        LOGGER.info("Closing COAP client");
        if (client != null) {
            Endpoint endpoint = client.getEndpoint();
            if (endpoint != null) {
                client.getEndpoint().destroy();
            }
            client.shutdown();
        }
        LOGGER.info("COAP client closed");
    }
}
