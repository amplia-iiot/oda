package es.amplia.oda.connector.websocket;

import es.amplia.oda.connector.websocket.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import org.java_websocket.client.WebSocketClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static es.amplia.oda.connector.websocket.WebSocketConnector.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectorTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 1234;
    private static final String TEST_PATH = "/path";
    private static final int TEST_CONNECTION_TIMEOUT = 5;
    private static final int TEST_KEEP_ALIVE_INTERVAL = 10;
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().host(TEST_HOST).port(TEST_PORT).path(TEST_PATH)
                    .connectionTimeout(TEST_CONNECTION_TIMEOUT).keepAliveInterval(TEST_KEEP_ALIVE_INTERVAL).build();
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_API_KEY = "apiKey";
    private static final byte[] TEST_PAYLOAD = "Test Message".getBytes();

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private WebSocketClientFactory mockedFactory;
    @InjectMocks
    private WebSocketConnector testConnector;

    @Mock
    private WebSocketClient mockedClient;
    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Test
    public void testLoadConfiguration() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        when(mockedFactory.createWebSocketClient(any(OpenGateConnector.class), any(URI.class), anyInt(), anyInt()))
                .thenReturn(mockedClient);

        testConnector.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedFactory).createWebSocketClient(eq(testConnector), uriCaptor.capture(), eq(TEST_CONNECTION_TIMEOUT),
                eq(TEST_KEEP_ALIVE_INTERVAL));
        URI uri = uriCaptor.getValue();
        assertEquals(WEBSOCKET_HEADER, uri.getScheme());
        assertEquals(TEST_HOST, uri.getHost());
        assertEquals(TEST_PORT, uri.getPort());
        assertEquals(TEST_PATH + "/" + TEST_DEVICE_ID, uri.getPath());
        assertEquals(API_KEY_PARAM + "=" + TEST_API_KEY, uri.getQuery());
        verify(mockedClient).connect();
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationNoDeviceId() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);

        testConnector.loadConfiguration(TEST_CONFIGURATION);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationNoApyKey() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(null);

        testConnector.loadConfiguration(TEST_CONFIGURATION);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationURISyntaxException() {
        ConnectorConfiguration invalidConfiguration =
                ConnectorConfiguration.builder().host("invalid?host").path("invalid/path").build();

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);

        testConnector.loadConfiguration(invalidConfiguration);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationConnectionException() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);

        doThrow(new IllegalStateException("")).when(mockedClient).connect();

        testConnector.loadConfiguration(TEST_CONFIGURATION);

        fail("Configuration exception must be thrown");
    }

    @Test
    public void testUplink() {
        Whitebox.setInternalState(testConnector, "client", mockedClient);

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).send(eq(TEST_PAYLOAD));
    }

    @Test
    public void testUplinkWithoutClient() {
        Whitebox.setInternalState(testConnector, "client", null);

        testConnector.uplink(TEST_PAYLOAD);
    }

    @Test
    public void testUplinkSendExceptionCaught() {
        Whitebox.setInternalState(testConnector, "client", mockedClient);

        doThrow(new RuntimeException()).when(mockedClient).send(any(byte[].class));

        testConnector.uplink(TEST_PAYLOAD);

        assertTrue("Exception is caught", true);
    }

    @Test
    public void testIsConnectedWithConnectedClient() {
        Whitebox.setInternalState(testConnector, "client", mockedClient);

        when(mockedClient.isOpen()).thenReturn(true);

        assertTrue(testConnector.isConnected());
    }

    @Test
    public void testIsConnectedWithNotConnectedClient() {
        Whitebox.setInternalState(testConnector, "client", mockedClient);

        when(mockedClient.isOpen()).thenReturn(false);

        assertFalse(testConnector.isConnected());
    }

    @Test
    public void testIsConnectedWithoutClient() {
        Whitebox.setInternalState(testConnector, "client", null);

        assertFalse(testConnector.isConnected());
    }

    @Test
    public void testCloseWithClient() {
        Whitebox.setInternalState(testConnector, "client", mockedClient);

        testConnector.close();

        verify(mockedClient).close();
    }

    @Test
    public void testCloseWithoutClient() {
        Whitebox.setInternalState(testConnector, "client", null);

        testConnector.close();
    }
}