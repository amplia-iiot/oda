package es.amplia.oda.comms.mqtt.api;

import lombok.Getter;

@Getter
public class MqttException extends RuntimeException {

    public static final short REASON_CODE_INVALID_PROTOCOL_VERSION		= 0x01;
    public static final short REASON_CODE_INVALID_CLIENT_ID      		= 0x02;
    public static final short REASON_CODE_BROKER_UNAVAILABLE             = 0x03;
    public static final short REASON_CODE_FAILED_AUTHENTICATION			= 0x04;
    public static final short REASON_CODE_NOT_AUTHORIZED				= 0x05;
    public static final short REASON_CODE_UNEXPECTED_ERROR				= 0x06;
    public static final short REASON_CODE_SUBSCRIBE_FAILED				= 0x80;
    public static final short REASON_CODE_CLIENT_TIMEOUT                = 32000;
    public static final short REASON_CODE_NO_MESSAGE_IDS_AVAILABLE      = 32001;
    public static final short REASON_CODE_WRITE_TIMEOUT                 = 32002;
    public static final short REASON_CODE_CLIENT_CONNECTED              = 32100;
    public static final short REASON_CODE_CLIENT_ALREADY_DISCONNECTED   = 32101;
    public static final short REASON_CODE_CLIENT_DISCONNECTING          = 32102;
    public static final short REASON_CODE_SERVER_CONNECT_ERROR          = 32103;
    public static final short REASON_CODE_CLIENT_NOT_CONNECTED          = 32104;
    public static final short REASON_CODE_SOCKET_FACTORY_MISMATCH       = 32105;
    public static final short REASON_CODE_SSL_CONFIG_ERROR              = 32106;
    public static final short REASON_CODE_CLIENT_DISCONNECT_PROHIBITED  = 32107;
    public static final short REASON_CODE_INVALID_MESSAGE				= 32108;
    public static final short REASON_CODE_CONNECTION_LOST               = 32109;
    public static final short REASON_CODE_CONNECT_IN_PROGRESS           = 32110;
    public static final short REASON_CODE_CLIENT_CLOSED		           = 32111;
    public static final short REASON_CODE_TOKEN_INUSE		           = 32201;
    public static final short REASON_CODE_MAX_INFLIGHT    			= 32202;
    public static final short REASON_CODE_DISCONNECTED_BUFFER_FULL	= 32203;

    private final int reasonCode;

    public MqttException(String message, int reasonCode) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public MqttException(String message, Throwable cause, int reasonCode) {
        super(message, cause);
        this.reasonCode = reasonCode;
    }
}
