package es.amplia.oda.core.commons.opcua;

public class OpcUaException extends RuntimeException {

    public OpcUaException() {}

    public OpcUaException(String message) {
        super(message);
    }

    public OpcUaException(String message, Throwable cause) {
        super(message, cause);
    }
}
