package es.amplia.oda.connector.coap;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.connector.coap.at.ATUDPConnector;
import es.amplia.oda.connector.coap.configuration.ConnectorConfiguration;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;

import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.ConnectorType;

class COAPClientFactory {

    static final String COAP_SCHEME = "coap";

    static final long MS_PER_SECOND = 1000;

    static final int API_KEY_OPTION_NUMBER = 2502;
    static final int DEVICE_ID_OPTION_NUMBER = 2503;
    static final int MESSAGE_PROTOCOL_VERSION_OPTION_NUMBER = 2504;

    private final DeviceInfoProvider deviceInfoProvider;
    private final ATManager atManager;

    COAPClientFactory(DeviceInfoProvider deviceInfoProvider, ATManager atManager) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.atManager = atManager;
    }

    CoapClient createClient(ConnectorConfiguration configuration) {
        CoapClient client = new CoapClient.Builder(configuration.getRemoteHost(), configuration.getRemotePort())
                                .scheme(COAP_SCHEME)
                                .path(configuration.getPath(), configuration.getProvisionPath())
                                .query()
                                .create();
        client.setTimeout(configuration.getTimeout() * MS_PER_SECOND);

        setConnectorFromType(client, configuration);

        return client;
    }

    private void setConnectorFromType(CoapClient client, ConnectorConfiguration configuration) {
        if (configuration.getType().equals(ConnectorType.AT)) {
            Connector atConnector =
                    new ATUDPConnector(atManager, configuration.getRemoteHost(), configuration.getRemotePort(),
                            configuration.getLocalPort());
            Endpoint endpoint = new CoapEndpoint(atConnector, NetworkConfig.getStandard());
            client.setEndpoint(endpoint);
        }
    }

    OptionSet createOptions(ConnectorConfiguration configuration) {
        String deviceId = deviceInfoProvider.getDeviceId();
        String apiKey = deviceInfoProvider.getApiKey();
        if (deviceId == null || apiKey == null) {
            throw new ConfigurationException("Device identifier or API key require to create the COAP connector are not available");
        }

        return new OptionSet()
                .addOption(new Option(API_KEY_OPTION_NUMBER, apiKey))
                .addOption(new Option(DEVICE_ID_OPTION_NUMBER, deviceId))
                .addOption(new Option(MESSAGE_PROTOCOL_VERSION_OPTION_NUMBER,
                                      configuration.getMessageProtocolVersion()));
    }
}
