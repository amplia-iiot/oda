package es.amplia.oda.hardware.modbus.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class TCPModbusMasterConfiguration {
    public static final int DEFAULT_PORT = 502;
    public static final int DEFAULT_TIMEOUT = 3000;
    public static final boolean DEFAULT_RECONNECTION = false;

    @NonNull
    private String address;
    @Builder.Default
    private int port = DEFAULT_PORT;
    @Builder.Default
    private int timeout = DEFAULT_TIMEOUT;
    @Builder.Default
    private boolean reconnect = DEFAULT_RECONNECTION;
}
