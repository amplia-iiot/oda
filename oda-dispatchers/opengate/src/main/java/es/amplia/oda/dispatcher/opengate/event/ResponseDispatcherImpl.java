package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.event.api.ResponseDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResponseDispatcherImpl implements ResponseDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherImpl.class);

    private final Serializer serializer;
    private final ContentType contentType;
    private final OpenGateConnector connector;

    public ResponseDispatcherImpl(Serializer serializer, ContentType contentType, OpenGateConnector connector) {
        this.serializer = serializer;
        this.contentType = contentType;
        this.connector = connector;
    }

    @Override
    public void publishResponse(OperationResponse response) {
        try {
            /*Output response = serializer.deserialize(output, Output.class);
            String deviceId = deviceInfoProvider.getDeviceId();
            List<String> path = Arrays.asList(response.getOperation().getResponse().getPath());
            path.add(0, deviceId);
            response.getOperation().getResponse().setPath((String[]) path.toArray());
            send(response);*/

            LOGGER.info("Publishing response {}", response);
            byte[] payload = serializer.serialize(response);
            connector.uplinkResponse(payload, contentType);
        } catch (IOException e) {
            LOGGER.error("Error serializing response {}. Response will not be published: ", response, e);
        }
    }

}
