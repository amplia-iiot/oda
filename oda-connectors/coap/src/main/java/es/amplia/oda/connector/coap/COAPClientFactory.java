package es.amplia.oda.connector.coap;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.connector.coap.at.ATUDPConnector;
import es.amplia.oda.connector.coap.configuration.ConnectorConfiguration;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.Connector;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Arrays;

import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.ConnectorType;

class COAPClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(COAPClientFactory.class);

    static final long MS_PER_SECOND = 1000;

    static final int API_KEY_OPTION_NUMBER = 2502;
    static final int DEVICE_ID_OPTION_NUMBER = 2503;
    static final int MESSAGE_PROTOCOL_VERSION_OPTION_NUMBER = 2504;

    private final DeviceInfoProvider deviceInfoProvider;
    private final ATManager atManager;

    COAPClientFactory(DeviceInfoProvider deviceInfoProvider, ATManager atManager) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.atManager = atManager;
        CaliforniumLogger.disableLogging();
    }

    CoapClient createClient(ConnectorConfiguration configuration) {
        CoapClient client = new CoapClient.Builder(configuration.getHost(), configuration.getPort())
                                .scheme(configuration.getScheme())
                                .path(configuration.getPath(), configuration.getProvisionPath())
                                .query()
                                .create();
        Connector connector = createConnectorFromConfiguration(configuration);
        Endpoint endpoint = new CoapEndpoint(connector, NetworkConfig.getStandard());
        endpoint.addInterceptor(new MessageLoggerInterceptor());
        client.setEndpoint(endpoint);
        client.setTimeout(configuration.getTimeout() * MS_PER_SECOND);

        return client;
    }

    private Connector createConnectorFromConfiguration(ConnectorConfiguration configuration) {
        if (configuration.getType().equals(ConnectorType.AT)) {
            return new ATUDPConnector(atManager, configuration.getHost(), configuration.getPort(),
                    configuration.getLocalPort());
        } else if (configuration.getType().equals(ConnectorType.DTLS)){
            try (FileInputStream keyStoreStream = new FileInputStream(configuration.getKeyStoreLocation());
                 FileInputStream trustStoreStream = new FileInputStream(configuration.getTrustStoreLocation())) {
                KeyStore keyStore = KeyStore.getInstance(configuration.getKeyStoreType());
                keyStore.load(keyStoreStream, configuration.getKeyStorePassword().toCharArray());

                KeyStore trustStore = KeyStore.getInstance(configuration.getTrustStoreType());
                trustStore.load(trustStoreStream, configuration.getTrustStorePassword().toCharArray());
                Certificate[] trustedCertificates =
                        Arrays.stream(configuration.getTrustedCertificates())
                                .map(certificateName -> getCertificateWithName(trustStore, certificateName.trim()))
                                .toArray(Certificate[]::new);

                InetSocketAddress inetSocketAddress = new InetSocketAddress(configuration.getLocalPort());
                DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder(inetSocketAddress);
                builder.setIdentity((PrivateKey) keyStore.getKey(configuration.getClientKeyAlias(), configuration.getKeyStorePassword().toCharArray()),
                        keyStore.getCertificateChain(configuration.getClientKeyAlias()), false);
                builder.setClientOnly();
                builder.setTrustStore(trustedCertificates);
                builder.setSupportedCipherSuites(new CipherSuite[]{ CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8 });

                return new DTLSConnector(builder.build());
            } catch (IllegalArgumentException | IllegalStateException | GeneralSecurityException | IOException ex) {
                LOGGER.error("Error creating CoAP DTLS Connector: {}", ex);
                throw new ConfigurationException("Error creating Coap Connector: Invalid configuration");
            }
        } else {
            UDPConnector udpConnector = new UDPConnector(new InetSocketAddress(configuration.getLocalPort()));
            NetworkConfig config = NetworkConfig.getStandard();

            udpConnector.setReceiverThreadCount(config.getInt(NetworkConfig.Keys.NETWORK_STAGE_RECEIVER_THREAD_COUNT));
            udpConnector.setSenderThreadCount(config.getInt(NetworkConfig.Keys.NETWORK_STAGE_SENDER_THREAD_COUNT));
            udpConnector.setReceiveBufferSize(config.getInt(NetworkConfig.Keys.UDP_CONNECTOR_RECEIVE_BUFFER));
            udpConnector.setSendBufferSize(config.getInt(NetworkConfig.Keys.UDP_CONNECTOR_SEND_BUFFER));
            udpConnector.setReceiverPacketSize(config.getInt(NetworkConfig.Keys.UDP_CONNECTOR_DATAGRAM_SIZE));

            return udpConnector;
        }
    }

    private Certificate getCertificateWithName(KeyStore trustStore, String certificateName) {
        try {
            return trustStore.getCertificate(certificateName);
        } catch (KeyStoreException exception) {
            LOGGER.error("Trusted certificate {} not found: {}", certificateName, exception);
            return null;
        }
    }

    OptionSet createOptions(ConnectorConfiguration configuration) {
        String deviceId = deviceInfoProvider.getDeviceId();
        String apiKey = deviceInfoProvider.getApiKey();
        if (deviceId == null || apiKey == null) {
            throw new ConfigurationException("Device identifier or API key require to create the COAP connector are not available");
        }

        return new OptionSet()
                .setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
                .addOption(new Option(API_KEY_OPTION_NUMBER, apiKey))
                .addOption(new Option(DEVICE_ID_OPTION_NUMBER, deviceId))
                .addOption(new Option(MESSAGE_PROTOCOL_VERSION_OPTION_NUMBER,
                        configuration.getMessageProtocolVersion()));
    }
}
