package es.amplia.oda.core.commons.snmp;

public class SnmpException extends RuntimeException {

    public SnmpException(String message) {
        super(message);
    }

    public SnmpException(String message, Throwable cause) {
        super(message, cause);
    }
}
