package es.amplia.oda.hardware.modbus.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TCPModbusMasterConfiguration {
    public static final int DEFAULT_PORT = 502;
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final boolean DEFAULT_NEW_CONNECTION_PER_REQUEST = false;

    @NonNull
    String address;
    @NonNull
    String deviceId;
    String deviceManufacturer;
    @Builder.Default
    int port = DEFAULT_PORT;
    @Builder.Default
    int timeout = DEFAULT_TIMEOUT;
    @Builder.Default
    boolean newConnPerRequest = DEFAULT_NEW_CONNECTION_PER_REQUEST;
}
