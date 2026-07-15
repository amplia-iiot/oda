package es.amplia.oda.connector.coap;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.connector.coap.at.ATUDPConnector;
import es.amplia.oda.connector.coap.configuration.ConnectorConfiguration;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import static es.amplia.oda.connector.coap.COAPClientFactory.*;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.COAP_SCHEME;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.COAP_SECURE_SCHEME;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.ConnectorType;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class COAPClientFactoryTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_API_KEY = "testApiKey";
    private static final String TEST_HOST = "host.com";
    private static final int TEST_PORT = 12345;
    private static final int TEST_LOCAL_PORT = 54321;
    private static final String TEST_PATH = "path/to/somewhere";
    private static final String TEST_PROVISION_PATH = "provision";
    private static final int TEST_TIMEOUT = 20;
    private static final String TEST_MESSAGE_PROTOCOL_VERSION = "1.2.3";
    private static final String TEST_KEY_STORE_TYPE = "PKCS12";
    private static final String TEST_KEY_STORE_LOCATION = "location/to/keystore";
    private static final String TEST_KEY_STORE_PASSWORD = "somePassword";
    private static final String TEST_CLIENT_KEY_ALIAS = "odaClient";
    private static final String TEST_TRUST_STORE_TYPE = "JCEKS";
    private static final String TEST_TRUST_STORE_LOCATION = "location/to/truststore";
    private static final String TEST_TRUST_STORE_PASSWORD = "anotherPassword";
    private static final String[] TEST_OPENGATE_CERTIFICATE_NAME = { "opengateCertificate", "letsEncryptCertificate" };
    private static final ConnectorConfiguration TEST_UDP_CONFIGURATION =
            ConnectorConfiguration.builder().scheme(COAP_SCHEME).host(TEST_HOST).port(TEST_PORT)
                    .localPort(TEST_LOCAL_PORT).path(TEST_PATH)
                    .provisionPath(TEST_PROVISION_PATH).timeout(TEST_TIMEOUT)
                    .messageProtocolVersion(TEST_MESSAGE_PROTOCOL_VERSION).build();
    private static final ConnectorConfiguration TEST_AT_CONFIGURATION =
            ConnectorConfiguration.builder().type(ConnectorType.AT).scheme(COAP_SCHEME).host(TEST_HOST)
                    .port(TEST_PORT).localPort(TEST_LOCAL_PORT).path(TEST_PATH)
                    .provisionPath(TEST_PROVISION_PATH).timeout(TEST_TIMEOUT)
                    .messageProtocolVersion(TEST_MESSAGE_PROTOCOL_VERSION).build();
    private static final ConnectorConfiguration TEST_DTLS_CONFIGURATION =
            ConnectorConfiguration.builder().type(ConnectorType.DTLS).scheme(COAP_SECURE_SCHEME).host(TEST_HOST)
                    .port(TEST_PORT).localPort(TEST_LOCAL_PORT).path(TEST_PATH)
                    .provisionPath(TEST_PROVISION_PATH).timeout(TEST_TIMEOUT)
                    .messageProtocolVersion(TEST_MESSAGE_PROTOCOL_VERSION)
                    .keyStoreType(TEST_KEY_STORE_TYPE).keyStoreLocation(TEST_KEY_STORE_LOCATION)
                    .keyStorePassword(TEST_KEY_STORE_PASSWORD).clientKeyAlias(TEST_CLIENT_KEY_ALIAS)
                    .trustStoreType(TEST_TRUST_STORE_TYPE).trustStoreLocation(TEST_TRUST_STORE_LOCATION)
                    .trustStorePassword(TEST_TRUST_STORE_PASSWORD)
                    .trustedCertificates(TEST_OPENGATE_CERTIFICATE_NAME).build();


    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private ATManager mockedATManager;
    @InjectMocks
    private COAPClientFactory testCoapClientFactory;


    @Test
    public void testCreateUDPClient() {
        List<List<?>> udpConnectorArgs = new ArrayList<>();
        List<List<?>> endpointArgs = new ArrayList<>();

        try (MockedConstruction<UDPConnector> udpConnectorCons =
                     mockConstruction(UDPConnector.class,
                             (mock, mctx) -> udpConnectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CoapEndpoint> endpointCons =
                     mockConstruction(CoapEndpoint.class,
                             (mock, mctx) -> endpointArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MessageLoggerInterceptor> loggerInterceptorCons =
                     mockConstruction(MessageLoggerInterceptor.class)) {

            CoapClient client = testCoapClientFactory.createClient(TEST_UDP_CONFIGURATION);

            assertNotNull(client);
            String uri = client.getURI();
            assertTrue(uri.contains(COAP_SCHEME));
            assertTrue(uri.contains(TEST_HOST));
            assertTrue(uri.contains(String.valueOf(TEST_PORT)));
            assertTrue(uri.contains(TEST_PATH));
            assertTrue(uri.contains(TEST_PROVISION_PATH));
            assertEquals(TEST_TIMEOUT * MS_PER_SECOND, client.getTimeout());
            assertEquals(1, udpConnectorCons.constructed().size());
            assertEquals(new InetSocketAddress(TEST_LOCAL_PORT), udpConnectorArgs.get(0).get(0));
            assertEquals(1, endpointCons.constructed().size());
            assertEquals(udpConnectorCons.constructed().get(0), endpointArgs.get(0).get(0));
            assertEquals(NetworkConfig.getStandard(), endpointArgs.get(0).get(1));
            assertEquals(1, loggerInterceptorCons.constructed().size());
            verify(endpointCons.constructed().get(0)).addInterceptor(eq(loggerInterceptorCons.constructed().get(0)));
            assertEquals(endpointCons.constructed().get(0), client.getEndpoint());
        }
    }

    @Test
    public void testCreateATClient() {
        List<List<?>> atUdpConnectorArgs = new ArrayList<>();
        List<List<?>> endpointArgs = new ArrayList<>();

        try (MockedConstruction<ATUDPConnector> atUdpConnectorCons =
                     mockConstruction(ATUDPConnector.class,
                             (mock, mctx) -> atUdpConnectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CoapEndpoint> endpointCons =
                     mockConstruction(CoapEndpoint.class,
                             (mock, mctx) -> endpointArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MessageLoggerInterceptor> loggerInterceptorCons =
                     mockConstruction(MessageLoggerInterceptor.class)) {

            CoapClient client = testCoapClientFactory.createClient(TEST_AT_CONFIGURATION);

            assertNotNull(client);
            String uri = client.getURI();
            assertTrue(uri.contains(COAP_SCHEME));
            assertTrue(uri.contains(TEST_HOST));
            assertTrue(uri.contains(String.valueOf(TEST_PORT)));
            assertTrue(uri.contains(TEST_PATH));
            assertTrue(uri.contains(TEST_PROVISION_PATH));
            assertEquals(TEST_TIMEOUT * MS_PER_SECOND, client.getTimeout());
            assertEquals(1, atUdpConnectorCons.constructed().size());
            assertEquals(mockedATManager, atUdpConnectorArgs.get(0).get(0));
            assertEquals(TEST_HOST, atUdpConnectorArgs.get(0).get(1));
            assertEquals(TEST_PORT, atUdpConnectorArgs.get(0).get(2));
            assertEquals(TEST_LOCAL_PORT, atUdpConnectorArgs.get(0).get(3));
            assertEquals(1, endpointCons.constructed().size());
            assertEquals(atUdpConnectorCons.constructed().get(0), endpointArgs.get(0).get(0));
            assertEquals(NetworkConfig.getStandard(), endpointArgs.get(0).get(1));
            assertEquals(1, loggerInterceptorCons.constructed().size());
            verify(endpointCons.constructed().get(0)).addInterceptor(eq(loggerInterceptorCons.constructed().get(0)));
            assertEquals(endpointCons.constructed().get(0), client.getEndpoint());
        }
    }

    @Test
    public void testCreateDTLSClient() throws Exception {
        KeyStore mockedKeyStore = mock(KeyStore.class);
        Certificate mockedCertificate = mock(Certificate.class);
        DtlsConnectorConfig mockedDtlsConfiguration = mock(DtlsConnectorConfig.class);

        List<List<?>> fileInputStreamArgs = new ArrayList<>();
        List<List<?>> builderArgs = new ArrayList<>();
        List<List<?>> dtlsConnectorArgs = new ArrayList<>();
        List<List<?>> endpointArgs = new ArrayList<>();

        try (MockedConstruction<FileInputStream> fileInputStreamCons =
                     mockConstruction(FileInputStream.class,
                             (mock, mctx) -> fileInputStreamArgs.add(new ArrayList<>(mctx.arguments())));
             MockedStatic<KeyStore> keyStoreStatic = mockStatic(KeyStore.class);
             MockedConstruction<DtlsConnectorConfig.Builder> builderCons =
                     mockConstruction(DtlsConnectorConfig.Builder.class, (mock, mctx) -> {
                         builderArgs.add(new ArrayList<>(mctx.arguments()));
                         when(mock.build()).thenReturn(mockedDtlsConfiguration);
                     });
             MockedConstruction<DTLSConnector> dtlsConnectorCons =
                     mockConstruction(DTLSConnector.class,
                             (mock, mctx) -> dtlsConnectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CoapEndpoint> endpointCons =
                     mockConstruction(CoapEndpoint.class,
                             (mock, mctx) -> endpointArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MessageLoggerInterceptor> loggerInterceptorCons =
                     mockConstruction(MessageLoggerInterceptor.class)) {

            keyStoreStatic.when(() -> KeyStore.getInstance(anyString())).thenReturn(mockedKeyStore);
            when(mockedKeyStore.getCertificate(anyString())).thenReturn(mockedCertificate);

            CoapClient client = testCoapClientFactory.createClient(TEST_DTLS_CONFIGURATION);

            assertNotNull(client);
            String uri = client.getURI();
            assertTrue(uri.contains(COAP_SCHEME));
            assertTrue(uri.contains(TEST_HOST));
            assertTrue(uri.contains(String.valueOf(TEST_PORT)));
            assertTrue(uri.contains(TEST_PATH));
            assertTrue(uri.contains(TEST_PROVISION_PATH));
            assertEquals(TEST_TIMEOUT * MS_PER_SECOND, client.getTimeout());

            assertEquals(2, fileInputStreamCons.constructed().size());
            assertEquals(TEST_KEY_STORE_LOCATION, fileInputStreamArgs.get(0).get(0));
            assertEquals(TEST_TRUST_STORE_LOCATION, fileInputStreamArgs.get(1).get(0));
            keyStoreStatic.verify(() -> KeyStore.getInstance(eq(TEST_KEY_STORE_TYPE)));
            keyStoreStatic.verify(() -> KeyStore.getInstance(eq(TEST_TRUST_STORE_TYPE)));
            verify(mockedKeyStore).load(eq(fileInputStreamCons.constructed().get(0)),
                    aryEq(TEST_KEY_STORE_PASSWORD.toCharArray()));
            verify(mockedKeyStore).load(eq(fileInputStreamCons.constructed().get(1)),
                    aryEq(TEST_TRUST_STORE_PASSWORD.toCharArray()));
            verify(mockedKeyStore).getCertificate(eq(TEST_OPENGATE_CERTIFICATE_NAME[0]));
            assertEquals(1, builderCons.constructed().size());
            assertEquals(new InetSocketAddress(TEST_LOCAL_PORT), builderArgs.get(0).get(0));
            DtlsConnectorConfig.Builder mockedBuilder = builderCons.constructed().get(0);
            verify(mockedBuilder).setClientOnly();
            verify(mockedBuilder).setTrustStore(aryEq(new Certificate[] {mockedCertificate, mockedCertificate}));
            verify(mockedBuilder).build();
            assertEquals(1, dtlsConnectorCons.constructed().size());
            assertEquals(mockedDtlsConfiguration, dtlsConnectorArgs.get(0).get(0));
            assertEquals(1, endpointCons.constructed().size());
            assertEquals(dtlsConnectorCons.constructed().get(0), endpointArgs.get(0).get(0));
            assertEquals(NetworkConfig.getStandard(), endpointArgs.get(0).get(1));
            assertEquals(1, loggerInterceptorCons.constructed().size());
            verify(endpointCons.constructed().get(0)).addInterceptor(eq(loggerInterceptorCons.constructed().get(0)));
            assertEquals(endpointCons.constructed().get(0), client.getEndpoint());
        }
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateDTLSClientIOException() {
        testCoapClientFactory.createClient(TEST_DTLS_CONFIGURATION);

        fail("Configuration Exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateDTLSClientGeneralSecurityException() throws Exception {
        KeyStore mockedKeyStore = mock(KeyStore.class);

        try (MockedConstruction<FileInputStream> fileInputStreamCons =
                     mockConstruction(FileInputStream.class);
             MockedStatic<KeyStore> keyStoreStatic = mockStatic(KeyStore.class)) {

            keyStoreStatic.when(() -> KeyStore.getInstance(anyString())).thenReturn(mockedKeyStore);
            doThrow(new NoSuchAlgorithmException()).when(mockedKeyStore)
                    .load(any(InputStream.class), any(char[].class));

            testCoapClientFactory.createClient(TEST_DTLS_CONFIGURATION);

            fail("Configuration exception must be thrown");
        }
    }

    @Test
    public void testCreateOptions() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);

        OptionSet optionSet = testCoapClientFactory.createOptions(TEST_UDP_CONFIGURATION);

        assertNotNull(optionSet);
        assertTrue(optionSet.asSortedList().stream()
                .anyMatch(option -> option.getNumber() == API_KEY_OPTION_NUMBER
                        && option.getStringValue().equals(TEST_API_KEY)));
        assertTrue(optionSet.asSortedList().stream()
                .anyMatch(option -> option.getNumber() == DEVICE_ID_OPTION_NUMBER
                        && option.getStringValue().equals(TEST_DEVICE_ID)));
        assertTrue(optionSet.asSortedList().stream()
                .anyMatch(option -> option.getNumber() == MESSAGE_PROTOCOL_VERSION_OPTION_NUMBER
                        && option.getStringValue().equals(TEST_MESSAGE_PROTOCOL_VERSION)));
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateOptionsNoDeviceId() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);

        testCoapClientFactory.createOptions(TEST_UDP_CONFIGURATION);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateOptionsNoApiKey() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(null);

        testCoapClientFactory.createOptions(TEST_UDP_CONFIGURATION);

        fail("Configuration exception must be thrown");
    }
}
