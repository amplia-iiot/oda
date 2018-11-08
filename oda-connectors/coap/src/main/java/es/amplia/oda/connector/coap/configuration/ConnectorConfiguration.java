package es.amplia.oda.connector.coap.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.eclipse.californium.core.network.config.NetworkConfig;

@Value
@Builder
public class ConnectorConfiguration {
    public enum ConnectorType {
        OS,
        AT
    }

    private ConnectorType type;
    @NonNull
    private String remoteHost;
    private int remotePort;
    @NonNull
    private String path;
    @NonNull
    private String provisionPath;
    private int localPort;
    private long timeout;
    private String messageProtocolVersion;

    public static ConnectorConfigurationBuilder builder() {
        return new DefaultConnectorConfigurationBuilder();
    }

    private static class DefaultConnectorConfigurationBuilder extends ConnectorConfigurationBuilder {

        private static final int DEFAULT_COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
        private static final int DEFAULT_LOCAL_PORT = 4123;
        private static final int DEFAULT_TIMEOUT = 30;
        private static final String DEFAULT_MESSAGE_PROTOCOL_VERSION = "1.0.0";

        DefaultConnectorConfigurationBuilder() {
            super.type = ConnectorType.OS;
            super.remotePort = DEFAULT_COAP_PORT;
            super.localPort = DEFAULT_LOCAL_PORT;
            super.timeout = DEFAULT_TIMEOUT;
            super.messageProtocolVersion = DEFAULT_MESSAGE_PROTOCOL_VERSION;
        }
    }
}
