package es.amplia.oda.connector.http.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ConnectorConfiguration {

    public static final int DEFAULT_PORT = 80;
    public static final boolean DEFAULT_COMPRESSION_ENABLED = false;
    public static final int DEFAULT_COMPRESSION_THRESHOLD = 512;

    @NonNull
    private String host;
    @Builder.Default
    private int port = DEFAULT_PORT;
    @NonNull
    private String generalPath;
    @NonNull
    private String collectionPath;
    @Builder.Default
    private boolean compressionEnabled = DEFAULT_COMPRESSION_ENABLED;
    @Builder.Default
    private int compressionThreshold = DEFAULT_COMPRESSION_THRESHOLD;
}
