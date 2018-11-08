package es.amplia.oda.core.commons.interfaces;

public interface ScadaConnector {

    <T, S> void uplink(int index, T value, S type, long timestamp);

    boolean isConnected();
}
