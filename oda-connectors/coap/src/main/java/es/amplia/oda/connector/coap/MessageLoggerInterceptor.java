package es.amplia.oda.connector.coap;

import org.eclipse.californium.core.coap.EmptyMessage;
import org.eclipse.californium.core.coap.Message;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MessageLoggerInterceptor implements MessageInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageLoggerInterceptor.class);

    static final String REQUEST_MESSAGE = "request";
    static final String RESPONSE_MESSAGE = "response";
    static final String EMPTY_MESSAGE = "empty message";

    @Override
    public void sendRequest(Request request) {
        logSendMessage(REQUEST_MESSAGE, request);
    }

    @Override
    public void sendResponse(Response response) {
        logSendMessage(RESPONSE_MESSAGE, response);
    }

    @Override
    public void sendEmptyMessage(EmptyMessage message) {
        logSendMessage(EMPTY_MESSAGE, message);
    }

    private void logSendMessage(String messageDescription, Message message) {
        LOGGER.info("Send {} to {}:{}", messageDescription, message.getDestination(), message.getDestinationPort());
        LOGGER.debug("Message content of {}: {}", messageDescription, message);
    }

    @Override
    public void receiveRequest(Request request) {
        logReceiveMessage(REQUEST_MESSAGE, request);
    }

    @Override
    public void receiveResponse(Response response) {
        logReceiveMessage(RESPONSE_MESSAGE, response);
    }

    @Override
    public void receiveEmptyMessage(EmptyMessage emptyMessage) {
        logReceiveMessage(EMPTY_MESSAGE, emptyMessage);
    }

    private void logReceiveMessage(String messageDescription, Message message) {
        LOGGER.info("Receive {} from {}:{}", messageDescription, message.getSource(), message.getSourcePort());
        LOGGER.debug("Message content of {}: {}", messageDescription, message);
    }
}
