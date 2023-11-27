package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.entities.ContentType;

public interface OpenGateConnector {
    void uplink(byte[] payload, ContentType contentType);
    void uplinkResponse(byte[] payload, ContentType contentType);
    boolean isConnected();

    default void uplink(byte[] payload) {
        uplink(payload, ContentType.JSON);
    }

    default void uplinkResponse(byte[] payload) {
        uplinkResponse(payload, ContentType.JSON);
    }

}
