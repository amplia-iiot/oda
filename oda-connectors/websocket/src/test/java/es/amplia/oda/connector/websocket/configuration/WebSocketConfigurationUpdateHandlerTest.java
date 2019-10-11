package es.amplia.oda.connector.websocket.configuration;

import es.amplia.oda.connector.websocket.WebSocketConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import static es.amplia.oda.connector.websocket.configuration.WebSocketConfigurationUpdateHandler.*;
import static es.amplia.oda.connector.websocket.configuration.ConnectorConfiguration.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketConfigurationUpdateHandlerTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 12345;
    private static final String TEST_PATH = "iot/data";
    private static final int TEST_CONNECTION_TIMEOUT = 5;
    private static final int TEST_KEEP_ALIVE_INTERVAL = 30;
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().host(TEST_HOST).port(TEST_PORT).path(TEST_PATH)
                    .connectionTimeout(TEST_CONNECTION_TIMEOUT).keepAliveInterval(TEST_KEEP_ALIVE_INTERVAL).build();


    @Mock
    private WebSocketConnector mockedConnector;
    @InjectMocks
    private WebSocketConfigurationUpdateHandler testConfigurationHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PORT_PROPERTY_NAME, Integer.toString(TEST_PORT));
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(CONNECTION_TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_CONNECTION_TIMEOUT));
        props.put(KEEP_ALIVE_INTERVAL_PROPERTY_NAME, Integer.toString(TEST_KEEP_ALIVE_INTERVAL));

        testConfigurationHandler.loadConfiguration(props);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testConfigurationHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadConfigurationDefaultValues() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);

        testConfigurationHandler.loadConfiguration(props);

        ConnectorConfiguration loadedConfiguration = (ConnectorConfiguration)
                Whitebox.getInternalState(testConfigurationHandler, "currentConfiguration");
        assertEquals(TEST_HOST, loadedConfiguration.getHost());
        assertEquals(DEFAULT_PORT, loadedConfiguration.getPort());
        assertEquals(TEST_PATH, loadedConfiguration.getPath());
        assertEquals(DEFAULT_CONNECTION_TIMEOUT, loadedConfiguration.getConnectionTimeout());
        assertEquals(DEFAULT_KEEP_ALIVE_INTERVAL, loadedConfiguration.getKeepAliveInterval());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationInvalid() {
        Dictionary<String, String> props = new Hashtable<>();

        testConfigurationHandler.loadConfiguration(props);

        fail("Illegal argument exception must be thrown");
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigurationHandler, "currentConfiguration", TEST_CONFIGURATION);

        testConfigurationHandler.applyConfiguration();

        verify(mockedConnector).loadConfiguration(eq(TEST_CONFIGURATION));
    }
}