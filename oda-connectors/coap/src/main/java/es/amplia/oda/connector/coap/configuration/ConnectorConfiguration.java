package es.amplia.oda.connector.coap.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.eclipse.californium.core.network.config.NetworkConfig;

@Value
@Builder
public class ConnectorConfiguration {

    public enum ConnectorType {
        UDP,
        DTLS,
        AT
    }

    public static final ConnectorType DEFAULT_CONNECTOR_TYPE = ConnectorType.UDP;
    public static final String COAP_SCHEME = "coap";
    public static final String COAP_SECURE_SCHEME = "coaps";
    public static final int DEFAULT_COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
    public static final int DEFAULT_COAP_SECURE_PORT =
            NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_SECURE_PORT);
    public static final int DEFAULT_LOCAL_PORT = 4123;
    public static final int DEFAULT_TIMEOUT = 30;
    public static final String DEFAULT_MESSAGE_PROTOCOL_VERSION = "1.0.0";
    public static final String DEFAULT_KEY_STORE_TYPE = "JKS";
    public static final String DEFAULT_CLIENT_KEY_ALIAS = "client";
    public static final String DEFAULT_OPENGATE_CERTIFICATE_ALIAS = "amplia";

    @Builder.Default
    private ConnectorType type = DEFAULT_CONNECTOR_TYPE;
    private String scheme;
    @NonNull
    private String host;
    private int port;
    @NonNull
    private String path;
    @NonNull
    private String provisionPath;
    @Builder.Default
    private int localPort = DEFAULT_LOCAL_PORT;
    @Builder.Default
    private long timeout = DEFAULT_TIMEOUT;
    @Builder.Default
    private String messageProtocolVersion = DEFAULT_MESSAGE_PROTOCOL_VERSION;
    @Builder.Default
    private String keyStoreType = DEFAULT_KEY_STORE_TYPE;
    private String keyStoreLocation;
    private String keyStorePassword;
    @Builder.Default
    private String clientKeyAlias = DEFAULT_CLIENT_KEY_ALIAS;
    @Builder.Default
    private String trustStoreType = DEFAULT_KEY_STORE_TYPE;
    private String trustStoreLocation;
    private String trustStorePassword;
    @Builder.Default
    private String openGateCertificateAlias = DEFAULT_OPENGATE_CERTIFICATE_ALIAS;
}
