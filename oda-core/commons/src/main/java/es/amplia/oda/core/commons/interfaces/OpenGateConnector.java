package es.amplia.oda.core.commons.interfaces;

public interface OpenGateConnector {

    void uplink(byte[] payload);

    boolean isConnected();
}
