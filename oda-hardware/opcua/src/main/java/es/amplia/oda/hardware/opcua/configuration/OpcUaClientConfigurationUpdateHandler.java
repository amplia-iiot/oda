package es.amplia.oda.hardware.opcua.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaTranslationInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.hardware.opcua.internal.OpcUaConnection;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem.ValueConsumer;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription.ItemCreationCallback;
import org.eclipse.milo.opcua.stack.client.security.ClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Stack;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.*;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class OpcUaClientConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClientConfigurationUpdateHandler.class);

    private static final String URL_PROPERTY_NAME = "url";
    private static final String PUBLISH_PROPERTY_NAME = "subscription.publish";
    private static final String DATAPOINTS_PROPERTY_NAME = "subscription.list";
    private static final String EMPTY_SCADA_TYPE = "*";

    private final OpcUaConnection internalConnection;
    private final EventDispatcher eventDispatcher;
    private final ScadaTableTranslator scadaTranslator;
    private String url;
    private Integer publish;
    private OpcUaClient client;
    private final List<MonitoredItemCreateRequest> requests = new ArrayList<>();
    private Long clientHandlerSeq;

    public OpcUaClientConfigurationUpdateHandler(EventDispatcher dispatcher, ScadaTableTranslatorProxy scadaTableTranslatorProxy, OpcUaConnection connection) {
        this.eventDispatcher = dispatcher;
        this.internalConnection = connection;
        this.scadaTranslator = scadaTableTranslatorProxy;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        close();
        url = Optional.ofNullable((String)props.get(URL_PROPERTY_NAME))
                            .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + URL_PROPERTY_NAME));
        publish = Optional.ofNullable((String)props.get(PUBLISH_PROPERTY_NAME)).map(Integer::parseInt)
                            .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + PUBLISH_PROPERTY_NAME));
        String subscriptionListStr = Optional.ofNullable((String)props.get(DATAPOINTS_PROPERTY_NAME))
                            .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + DATAPOINTS_PROPERTY_NAME));
        
        String[] arrayDp = subscriptionListStr.split("\\|");
        for (int i = 0; i < arrayDp.length; i++) {
            String[] meterParams = arrayDp[i].split(";");
            if (meterParams.length != 3) throw new ConfigurationException("Invalid meter configuration: " + arrayDp[i] + ", must be namespaceIndex;nodeId;samplingInterval");
            Integer namespaceIndex = Integer.parseInt(meterParams[0]);
            Integer nodeId = Integer.parseInt(meterParams[1]);
            Double samplingInterval = Double.parseDouble(meterParams[1]);
            requests.add(createRequest(namespaceIndex, nodeId, samplingInterval));
        }
                    
        LOGGER.info("New configuration loaded");
    }

    private MonitoredItemCreateRequest createRequest(Integer namespaceIndex, Integer nodeId, Double samplingInterval) {
        ReadValueId readValueId = new ReadValueId(new NodeId(namespaceIndex, uint(nodeId)),
                AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE
        );

        MonitoringParameters parameters = new MonitoringParameters(
                UInteger.valueOf(clientHandlerSeq++),
                samplingInterval,     // sampling interval
                null,       // filter, null means use default
                UInteger.valueOf(10),   // queue size
                true        // discard oldest
        );

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                readValueId,
                MonitoringMode.Reporting,
                parameters
        );

        return request;
    }

    @Override
    public void applyConfiguration() {
        try {
            client = createSecureClient();
            if (client == null) {
                return;
            }

            client.connect().get();
            internalConnection.setClient(client);
            UaSubscription subscription = client.getSubscriptionManager().createSubscription(publish).get();

            List<UaMonitoredItem> items = subscription.createMonitoredItems(TimestampsToReturn.Both, requests,
                    new ItemCreationCallback() {
                        @Override
                        public void onItemCreated(UaMonitoredItem item, int index) {
                            item.setValueConsumer(new ValueConsumer() {

                                @Override
                                public void onValueArrived(UaMonitoredItem item, DataValue value) {
                                    NodeId nodeIdRead = item.getReadValueId().getNodeId();
                                    UInteger nodeId = (UInteger) nodeIdRead.getIdentifier();
                                    UShort namespaceIndex = nodeIdRead.getNamespaceIndex();
                                    LOGGER.info("Value received: namespaceIndex = {}, nodeId = {}, value = {}",
                                            namespaceIndex, nodeId, value);
                                    ScadaTranslationInfo dsInfo = scadaTranslator.getTranslationInfo(
                                            new ScadaInfo(nodeId.intValue(), EMPTY_SCADA_TYPE), false);
                                    Event e = new Event(dsInfo.getDatastreamId(), dsInfo.getDeviceId(), null,
                                            dsInfo.getFeed(), System.currentTimeMillis(), value.getValue().getValue());
                                    LOGGER.info("Publishing event: " + e);
                                    eventDispatcher.publish(Collections.singletonList(e));
                                }

                            });
                        }
                    }
            ).get();

            for (UaMonitoredItem item : items) {
                if (item.getStatusCode().isGood()) {
                    LOGGER.info("Item created for for namespaceIndex {}, nodeId {}",
                            item.getReadValueId().getNodeId().getNamespaceIndex(),
                            item.getReadValueId().getNodeId().getIdentifier());
                } else {
                    LOGGER.error("Failed to create item for namespaceIndex {}, nodeId {} : (status {})",
                            item.getReadValueId().getNodeId().getNamespaceIndex(),
                            item.getReadValueId().getNodeId().getIdentifier(), item.getStatusCode());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception =  ", e);
        }
    }

    public void close() {
        if (client != null) {
            client.getSubscriptionManager().cancelWatchdogTimers();
            client.disconnect();
        }
        requests.clear();
        clientHandlerSeq = 0L;
    }

    private OpcUaClient createSecureClient() {

        try {
            File serverCertFile = new File("certs/serverCert.der");
            File clientCertFile = new File("certs/my_cert.pem");
            File publicKeyFile = new File("certs/clientPublicKey.pem");
            File privateKeyFile = new File("certs/my_private_key.pem");

            KeyPair clientKeyPair = new KeyPair(readX509PublicKey(publicKeyFile), readPKCS8PrivateKey(privateKeyFile));

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate serverCert = cf.generateCertificate(Files.newInputStream(serverCertFile.toPath()));
            Certificate clientCert = cf.generateCertificate(Files.newInputStream(clientCertFile.toPath()));

            ApplicationDescription description = new ApplicationDescription("urn:open62541.server.application",
                    "http://open62541.org", LocalizedText.english("TestAmplia"),
                    ApplicationType.Client, url, "opc.tcp://aquaviewportal.nl:4840",
                    new String[]{"opc.tcp://aquaviewportal.nl:4840"});

            UserTokenPolicy userPolicy = new UserTokenPolicy("open62541-username-policy", UserTokenType.UserName,
                    null, null,
                    "http://opcfoundation.org/UA/SecurityPolicy#Aes128_Sha256_RsaOaep");

            EndpointDescription endpoint = EndpointDescription.builder()
                    .securityMode(MessageSecurityMode.SignAndEncrypt)
                    .securityPolicyUri(SecurityPolicy.Basic256Sha256.getUri())
                    .endpointUrl(url)
                    .transportProfileUri(Stack.TCP_UASC_UABINARY_TRANSPORT_URI)
                    .serverCertificate(new ByteString(serverCert.getEncoded()))
                    .server(description)
                    .userIdentityTokens(new UserTokenPolicy[]{userPolicy})
                    .build();
            OpcUaClientConfig clientConfig = OpcUaClientConfig.builder()
                    .setIdentityProvider(new UsernameProvider("OPC_UA_Consumer", "Ip7fA1V42GCm2"))
                    .setEndpoint(endpoint)
                    .setKeyPair(clientKeyPair)
                    .setCertificate((X509Certificate) clientCert)
                    .setCertificateValidator(new ClientCertificateValidator.InsecureValidator())
                    .setApplicationUri("urn:open62541.server.application")
                    .setApplicationName(LocalizedText.english("TestAmplia"))
                    .build();

            return OpcUaClient.create(clientConfig);
        } catch (Exception e) {
            LOGGER.error("Error creating opcua client = ", e);
            return null;
        }
    }


    private RSAPublicKey readX509PublicKey(File file) throws Exception {
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

        //LOGGER.info("Public key before {}", key);

        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        //LOGGER.info("Public key after {}", publicKeyPEM);

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private RSAPrivateKey readPKCS8PrivateKey(File file) throws Exception {
        String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

        //LOGGER.info("Private key before {}", key);

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        //LOGGER.info("Private key after {}", privateKeyPEM);

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
