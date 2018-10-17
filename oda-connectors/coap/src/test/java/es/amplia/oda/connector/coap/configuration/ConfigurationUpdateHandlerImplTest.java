package es.amplia.oda.connector.coap.configuration;

import es.amplia.oda.connector.coap.COAPConnector;

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
import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.ConnectorType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationUpdateHandlerImplTest {

    private static final String TEST_TYPE = "AT";
    private static final String TEST_REMOTE_HOST = "host.com";
    private static final int TEST_REMOTE_PORT = 12345;
    private static final String TEST_PATH = "path/to/somewhere";
    private static final String TEST_PROVISION_PATH = "provision";
    private static final int TEST_LOCAL_PORT = 4567;
    private static final int TEST_TIMEOUT = 25;
    private static final String TEST_MESSAGE_PROTOCOL_VERSION = "1.2.3";
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().type(ConnectorType.AT).remoteHost(TEST_REMOTE_HOST)
                    .remotePort(TEST_REMOTE_PORT).path(TEST_PATH).provisionPath(TEST_PROVISION_PATH)
                    .localPort(TEST_LOCAL_PORT).timeout(TEST_TIMEOUT)
                    .messageProtocolVersion(TEST_MESSAGE_PROTOCOL_VERSION).build();

    private static final ConnectorType DEFAULT_TYPE = ConnectorType.OS;
    private static final int DEFAULT_LOCAL_PORT = 4123;
    private static final int DEFAULT_TIMEOUT = 30;
    private static final String DEFAULT_MESSAGE_PROTOCOL_VERSION = "1.0.0";

    @Mock
    private COAPConnector mockedConnector;
    @InjectMocks
    private ConfigurationUpdateHandlerImpl testConfigHandler;

    @Test
    public void testLoadConfigurationComplete() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, TEST_TYPE);
        props.put(REMOTE_HOST_PROPERTY_NAME, TEST_REMOTE_HOST);
        props.put(REMOTE_PORT_PROPERTY_NAME, String.valueOf(TEST_REMOTE_PORT));
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
        props.put(REMOTE_HOST_PROPERTY_NAME, TEST_REMOTE_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        ConnectorConfiguration conf = Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
        assertEquals(DEFAULT_TYPE, conf.getType());
        assertEquals(NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT), conf.getRemotePort());
        assertEquals(DEFAULT_LOCAL_PORT, conf.getLocalPort());
        assertEquals(DEFAULT_TIMEOUT, conf.getTimeout());
        assertEquals(DEFAULT_MESSAGE_PROTOCOL_VERSION, conf.getMessageProtocolVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationMissingRequiredProperty() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(REMOTE_HOST_PROPERTY_NAME, TEST_REMOTE_HOST);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        fail("Illegal Argument Exception must be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationInvalidType() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(CONNECTOR_TYPE_PROPERTY_NAME, "INVALID");
        props.put(REMOTE_HOST_PROPERTY_NAME, TEST_REMOTE_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        fail("Illegal Argument Exception must be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationInvalidPort() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(REMOTE_HOST_PROPERTY_NAME, TEST_REMOTE_HOST);
        props.put(REMOTE_PORT_PROPERTY_NAME, "INVALID");
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(PROVISION_PATH_PROPERTY_NAME, TEST_PROVISION_PATH);

        testConfigHandler.loadConfiguration(props);

        fail("Illegal Argument Exception must be thrown");
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigHandler, "connector", mockedConnector);
        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", TEST_CONFIGURATION);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadAndInit(eq(TEST_CONFIGURATION));
    }
}