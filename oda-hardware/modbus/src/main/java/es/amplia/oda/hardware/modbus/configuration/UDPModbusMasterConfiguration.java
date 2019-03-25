package es.amplia.oda.hardware.modbus.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UDPModbusMasterConfiguration {
    public static final int DEFAULT_PORT = 502;
    public static final int DEFAULT_TIMEOUT = 3000;

    @NonNull
    private String address;
    @Builder.Default
    private int port = DEFAULT_PORT;
    @Builder.Default
    private int timeout = DEFAULT_TIMEOUT;
}
