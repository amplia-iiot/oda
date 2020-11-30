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
    String address;
    @Builder.Default
    int port = DEFAULT_PORT;
    @Builder.Default
    int timeout = DEFAULT_TIMEOUT;
}
