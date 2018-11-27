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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;

import static es.amplia.oda.connector.coap.COAPClientFactory.*;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.COAP_SCHEME;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.COAP_SECURE_SCHEME;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.ConnectorType;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ COAPClientFactory.class, KeyStore.class, DtlsConnectorConfig.Builder.class })
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
    private static final String TEST_OPENGATE_CERTIFICATE_NAME = "opengateCertificate";
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
                    .openGateCertificateAlias(TEST_OPENGATE_CERTIFICATE_NAME).build();


    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private ATManager mockedATManager;
    @InjectMocks
    private COAPClientFactory testCoapClientFactory;

    @Mock
    private InetSocketAddress mockedAddress;
    @Mock
    private ATUDPConnector mockedATUDPConnector;
    @Mock
    private CoapEndpoint mockedEndpoint;


    @Test
    public void testCreateUDPClient() throws Exception {
        UDPConnector mockedUdpConnector = mock(UDPConnector.class);

        PowerMockito.whenNew(InetSocketAddress.class).withAnyArguments().thenReturn(mockedAddress);
        PowerMockito.whenNew(UDPConnector.class).withAnyArguments().thenReturn(mockedUdpConnector);

        CoapClient client = testCoapClientFactory.createClient(TEST_UDP_CONFIGURATION);

        assertNotNull(client);
        String uri = client.getURI();
        assertTrue(uri.contains(COAP_SCHEME));
        assertTrue(uri.contains(TEST_HOST));
        assertTrue(uri.contains(String.valueOf(TEST_PORT)));
        assertTrue(uri.contains(TEST_PATH));
        assertTrue(uri.contains(TEST_PROVISION_PATH));
        assertEquals(TEST_TIMEOUT * MS_PER_SECOND, client.getTimeout());
        PowerMockito.verifyNew(InetSocketAddress.class).withArguments(eq(TEST_LOCAL_PORT));
        PowerMockito.verifyNew(UDPConnector.class).withArguments(eq(mockedAddress));
    }

    @Test
    public void testCreateATClient() throws Exception {
        PowerMockito.whenNew(ATUDPConnector.class).withAnyArguments().thenReturn(mockedATUDPConnector);
        PowerMockito.whenNew(CoapEndpoint.class).withAnyArguments().thenReturn(mockedEndpoint);

        CoapClient client = testCoapClientFactory.createClient(TEST_AT_CONFIGURATION);

        assertNotNull(client);
        String uri = client.getURI();
        assertTrue(uri.contains(COAP_SCHEME));
        assertTrue(uri.contains(TEST_HOST));
        assertTrue(uri.contains(String.valueOf(TEST_PORT)));
        assertTrue(uri.contains(TEST_PATH));
        assertTrue(uri.contains(TEST_PROVISION_PATH));
        assertEquals(TEST_TIMEOUT * MS_PER_SECOND, client.getTimeout());
        PowerMockito.verifyNew(ATUDPConnector.class).withArguments(eq(mockedATManager), eq(TEST_HOST), eq(TEST_PORT),
                eq(TEST_LOCAL_PORT));
        PowerMockito.verifyNew(CoapEndpoint.class).withArguments(eq(mockedATUDPConnector), eq(NetworkConfig.getStandard()));
        assertEquals(mockedEndpoint, client.getEndpoint());
    }

    @Test
    public void testCreateDTLSClient() throws Exception {
        FileInputStream mockedFileInputStream = mock(FileInputStream.class);
        KeyStore mockedKeyStore = PowerMockito.mock(KeyStore.class);
        Certificate mockedCertificate = mock(Certificate.class);
        DtlsConnectorConfig.Builder mockedBuilder = PowerMockito.mock(DtlsConnectorConfig.Builder.class);
        DTLSConnector mockedDtlsConnector = mock(DTLSConnector.class);
        DtlsConnectorConfig mockedDtlsConfiguration = mock(DtlsConnectorConfig.class);

        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(mockedFileInputStream);
        PowerMockito.mockStatic(KeyStore.class);
        when(KeyStore.getInstance(anyString())).thenReturn(mockedKeyStore).thenReturn(mockedKeyStore);
        when(mockedKeyStore.getCertificate(anyString())).thenReturn(mockedCertificate);
        PowerMockito.whenNew(InetSocketAddress.class).withAnyArguments().thenReturn(mockedAddress);
        PowerMockito.whenNew(DtlsConnectorConfig.Builder.class).withAnyArguments().thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedDtlsConfiguration);
        PowerMockito.whenNew(DTLSConnector.class).withAnyArguments().thenReturn(mockedDtlsConnector);
        PowerMockito.whenNew(CoapEndpoint.class).withAnyArguments().thenReturn(mockedEndpoint);

        CoapClient client = testCoapClientFactory.createClient(TEST_DTLS_CONFIGURATION);

        assertNotNull(client);
        String uri = client.getURI();
        assertTrue(uri.contains(COAP_SCHEME));
        assertTrue(uri.contains(TEST_HOST));
        assertTrue(uri.contains(String.valueOf(TEST_PORT)));
        assertTrue(uri.contains(TEST_PATH));
        assertTrue(uri.contains(TEST_PROVISION_PATH));
        assertEquals(TEST_TIMEOUT * MS_PER_SECOND, client.getTimeout());

        PowerMockito.verifyNew(FileInputStream.class).withArguments(eq(TEST_KEY_STORE_LOCATION));
        PowerMockito.verifyNew(FileInputStream.class).withArguments(eq(TEST_TRUST_STORE_LOCATION));
        PowerMockito.verifyStatic(KeyStore.class);
        KeyStore.getInstance(eq(TEST_KEY_STORE_TYPE));
        KeyStore.getInstance(eq(TEST_TRUST_STORE_TYPE));
        verify(mockedKeyStore).load(eq(mockedFileInputStream), aryEq(TEST_KEY_STORE_PASSWORD.toCharArray()));
        verify(mockedKeyStore).load(eq(mockedFileInputStream), aryEq(TEST_TRUST_STORE_PASSWORD.toCharArray()));
        verify(mockedKeyStore).getCertificate(eq(TEST_OPENGATE_CERTIFICATE_NAME));
        PowerMockito.verifyNew(InetSocketAddress.class).withArguments(eq(TEST_LOCAL_PORT));
        PowerMockito.verifyNew(DtlsConnectorConfig.Builder.class).withArguments(eq(mockedAddress));
        verify(mockedBuilder).setClientOnly();
        verify(mockedBuilder).setTrustStore(aryEq(new Certificate[] {mockedCertificate}));
        verify(mockedBuilder).build();
        PowerMockito.verifyNew(DTLSConnector.class).withArguments(eq(mockedDtlsConfiguration));
        PowerMockito.verifyNew(CoapEndpoint.class)
                .withArguments(eq(mockedDtlsConnector), eq(NetworkConfig.getStandard()));
        assertEquals(mockedEndpoint, client.getEndpoint());
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateDTLSClientIOException() throws Exception {
        PowerMockito.whenNew(FileInputStream.class).withParameterTypes(String.class)
                .withArguments(eq(TEST_KEY_STORE_LOCATION)).thenThrow(new FileNotFoundException());

        testCoapClientFactory.createClient(TEST_DTLS_CONFIGURATION);

        fail("Configuration Exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateDTLSClientGeneralSecurityException() throws Exception {
        FileInputStream mockedFileInputStream = mock(FileInputStream.class);
        KeyStore mockedKeyStore = PowerMockito.mock(KeyStore.class);

        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(mockedFileInputStream);
        PowerMockito.mockStatic(KeyStore.class);
        when(KeyStore.getInstance(anyString())).thenReturn(mockedKeyStore).thenReturn(mockedKeyStore);
        doThrow(new NoSuchAlgorithmException()).when(mockedKeyStore).load(any(InputStream.class), any(char[].class));

        testCoapClientFactory.createClient(TEST_DTLS_CONFIGURATION);

        fail("Configuration exception must be thrown");
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