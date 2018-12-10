package es.amplia.oda.connector.coap.configuration;

import es.amplia.oda.connector.coap.COAPConnector;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import org.eclipse.californium.core.network.config.NetworkConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.connector.coap.configuration.ConfigurationUpdateHandlerImpl.*;
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationUpdateHandlerImplTest {

    private static final String TEST_TYPE = "AT";
    private static final String TEST_HOST = "host.com";
    private static final int TEST_PORT = 12345;
    private static final String TEST_PATH = "path/to/somewhere";
    private static final String TEST_PROVISION_PATH = "provision";
    private static final int TEST_LOCAL_PORT = 4567;
    private static final int TEST_TIMEOUT = 25;
    private static final String TEST_MESSAGE_PROTOCOL_VERSION = "1.2.3";
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().type(ConnectorType.AT).scheme(COAP_SCHEME).host(TEST_HOST)
                    .port(TEST_PORT).path(TEST_PATH).provisionPath(TEST_PROVISION_PATH)
                    .localPort(TEST_LOCAL_PORT).timeout(TEST_TIMEOUT)
                    .messageProtocolVersion(TEST_MESSAGE_PROTOCOL_VERSION).build();
    private static final String TEST_KEY_STORE_TYPE = "JCEKS";
    private static final String TEST_KEY_STORE_LOCATION = "location/to/keystore";
    private static final String TEST_KEY_STORE_PASSWORD = "somePassword";
    private static final String TEST_CLIENT_KEY_ALIAS = "clientAlias";
    private static final String TEST_TRUST_STORE_TYPE = "PKCS12";
    private static final String TEST_TRUST_STORE_LOCATION = "location/to/truststore";
    private static final String TEST_TRUST_STORE_PASSWORD = "anotherPassword";
    private static final String[] TEST_TRUSTED_CERTIFICATES = {"opengateCertificate","othertrustedcertificate"};

    @Mock
    private COAPConnector mockedConnector;
    @InjectMocks
    private ConfigurationUpdateHandlerImpl testConfigHandler;

    @Test
    public void testLoadConfigurationComplete() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, TEST_TYPE);
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PORT_PROPERTY_NAME, String.valueOf(TEST_PORT));
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);
        props.put(LOCAL_PORT_PROPERTY_NAME, String.valueOf(TEST_LOCAL_PORT));
        props.put(TIMEOUT_PROPERTY_NAME, String.valueOf(TEST_TIMEOUT));
        props.put(MESSAGE_PROTOCOL_VERSION_PROPERTY_NAME, TEST_MESSAGE_PROTOCOL_VERSION);

        testConfigHandler.loadConfiguration(props);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadConfigurationDefaultValues() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        ConnectorConfiguration conf = Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
        assertEquals(COAP_SCHEME, conf.getScheme());
        assertEquals(DEFAULT_CONNECTOR_TYPE, conf.getType());
        assertEquals(NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT), conf.getPort());
        assertEquals(DEFAULT_LOCAL_PORT, conf.getLocalPort());
        assertEquals(DEFAULT_TIMEOUT, conf.getTimeout());
        assertEquals(DEFAULT_MESSAGE_PROTOCOL_VERSION, conf.getMessageProtocolVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationMissingRequiredProperty() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        fail("Illegal Argument Exception must be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationInvalidType() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, "INVALID");
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        fail("Illegal Argument Exception must be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationInvalidPort() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PORT_PROPERTY_NAME, "INVALID");
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        fail("Illegal Argument Exception must be thrown");
    }

    @Test
    public void testLoadConfigurationDTLSCompleteConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, ConnectorType.DTLS.toString());
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PORT_PROPERTY_NAME, Integer.toString(TEST_PORT));
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);
        props.put(KEY_STORE_TYPE_PROPERTY_NAME, TEST_KEY_STORE_TYPE);
        props.put(KEY_STORE_LOCATION_PROPERTY_NAME, TEST_KEY_STORE_LOCATION);
        props.put(KEY_STORE_PASSWORD_PROPERTY_NAME, TEST_KEY_STORE_PASSWORD);
        props.put(CLIENT_KEY_ALIAS_PROPERTY_NAME, TEST_CLIENT_KEY_ALIAS);
        props.put(TRUST_STORE_TYPE_PROPERTY_NAME, TEST_TRUST_STORE_TYPE);
        props.put(TRUST_STORE_LOCATION_PROPERTY_NAME, TEST_TRUST_STORE_LOCATION);
        props.put(TRUST_STORE_PASSWORD_PROPERTY_NAME, TEST_TRUST_STORE_PASSWORD);
        props.put(TRUSTED_CERTIFICATES_PROPERTY_NAME, String.join(TRUSTED_CERTIFICATES_SEPARATOR, TEST_TRUSTED_CERTIFICATES));

        testConfigHandler.loadConfiguration(props);

        ConnectorConfiguration conf = Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
        assertEquals(ConnectorType.DTLS, conf.getType());
        assertEquals(COAP_SECURE_SCHEME, conf.getScheme());
        assertEquals(TEST_PORT, conf.getPort());
        assertEquals(TEST_TRUST_STORE_TYPE, conf.getTrustStoreType());
        assertEquals(TEST_TRUST_STORE_LOCATION, conf.getTrustStoreLocation());
        assertEquals(TEST_TRUST_STORE_PASSWORD, conf.getTrustStorePassword());
        assertArrayEquals(TEST_TRUSTED_CERTIFICATES, conf.getTrustedCertificates());
    }

    @Test
    public void testLoadConfigurationDTLSRequiredConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, ConnectorType.DTLS.toString());
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);
        props.put(KEY_STORE_LOCATION_PROPERTY_NAME, TEST_KEY_STORE_LOCATION);
        props.put(KEY_STORE_PASSWORD_PROPERTY_NAME, TEST_KEY_STORE_PASSWORD);
        props.put(TRUST_STORE_LOCATION_PROPERTY_NAME, TEST_TRUST_STORE_LOCATION);
        props.put(TRUST_STORE_PASSWORD_PROPERTY_NAME, TEST_TRUST_STORE_PASSWORD);
        props.put(TRUSTED_CERTIFICATES_PROPERTY_NAME, String.join(TRUSTED_CERTIFICATES_SEPARATOR, TEST_TRUSTED_CERTIFICATES));

        testConfigHandler.loadConfiguration(props);

        ConnectorConfiguration conf = Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
        assertEquals(ConnectorType.DTLS, conf.getType());
        assertEquals(COAP_SECURE_SCHEME, conf.getScheme());
        assertEquals(DEFAULT_COAP_SECURE_PORT, conf.getPort());
        assertEquals(DEFAULT_KEY_STORE_TYPE, conf.getKeyStoreType());
        assertEquals(TEST_KEY_STORE_LOCATION, conf.getKeyStoreLocation());
        assertEquals(TEST_KEY_STORE_PASSWORD, conf.getKeyStorePassword());
        assertEquals(DEFAULT_CLIENT_KEY_ALIAS, conf.getClientKeyAlias());
        assertEquals(DEFAULT_KEY_STORE_TYPE, conf.getTrustStoreType());
        assertEquals(TEST_TRUST_STORE_LOCATION, conf.getTrustStoreLocation());
        assertEquals(TEST_TRUST_STORE_PASSWORD, conf.getTrustStorePassword());
        assertArrayEquals(TEST_TRUSTED_CERTIFICATES, conf.getTrustedCertificates());
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationDTLSInvalidConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, ConnectorType.DTLS.toString());
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);
        props.put(KEY_STORE_LOCATION_PROPERTY_NAME, TEST_KEY_STORE_LOCATION);
        props.put(TRUST_STORE_LOCATION_PROPERTY_NAME, TEST_TRUST_STORE_LOCATION);

        testConfigHandler.loadConfiguration(props);

        fail("Configuration exception must be thrown because trust store password is missing");
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigHandler, "connector", mockedConnector);
        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", TEST_CONFIGURATION);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadAndInit(eq(TEST_CONFIGURATION));
    }

    @Test
    public void testApplyConfigurationNoCurrentConfiguration() {
        Whitebox.setInternalState(testConfigHandler, "connector", mockedConnector);
        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", (ConnectorConfiguration) null);

        testConfigHandler.applyConfiguration();

        verifyZeroInteractions(mockedConnector);
    }
}