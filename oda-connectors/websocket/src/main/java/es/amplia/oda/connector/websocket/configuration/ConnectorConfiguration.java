package es.amplia.oda.connector.websocket.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ConnectorConfiguration {
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30;
    public static final int DEFAULT_KEEPALIVE_INTERVAL = 180;

    @NonNull
    private String host;
    @Builder.Default
    private int port = DEFAULT_PORT;
    @NonNull
    private String path;
    @Builder.Default
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    @Builder.Default
    private int keepAliveInterval = DEFAULT_KEEPALIVE_INTERVAL;
}
