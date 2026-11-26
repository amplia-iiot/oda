package es.amplia.oda.connector.coap;

import es.amplia.oda.connector.coap.configuration.ConnectorConfiguration;

import es.amplia.oda.core.commons.entities.ContentType;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import static es.amplia.oda.connector.coap.COAPConnector.UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class COAPConnectorTest {

    private static final String COAP_SCHEME = "coap";
    private static final String TEST_HOST = "host.com";
    private static final String TEST_PATH = "path/to/somewhere";
    private static final String TEST_PROVISION_PATH = "provision";
    private static final byte[] TEST_PAYLOAD = "Test message".getBytes();
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().scheme(COAP_SCHEME).host(TEST_HOST).path(TEST_PATH)
                    .provisionPath(TEST_PROVISION_PATH).build();

    private static final String NO_EXCEPTION_THROWN_MESSAGE = "No exception is thrown";
    private static final String CLIENT_FIELD_NAME = "client";
    private static final String OPTION_SET_FIELD_NAME = "optionSet";

    @Mock
    private COAPClientFactory mockedCOAPClientFactory;
    @InjectMocks
    private COAPConnector testConnector;

    @Mock
    private CoapClient mockedClient;
    @Mock
    private Endpoint mockedEndpoint;
    @Mock
    private OptionSet mockedOptionSet;
    @Mock
    private Request mockedRequest;
    @Mock
    private CoapResponse mockedResponse;


    @Test
    public void testLoadAndInit() {Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, (Object) null);
        when(mockedCOAPClientFactory.createClient(any(ConnectorConfiguration.class)))
                .thenReturn(mockedClient);
        when(mockedCOAPClientFactory.createOptions(any(ConnectorConfiguration.class)))
                .thenReturn(mockedOptionSet);

        testConnector.loadAndInit(TEST_CONFIGURATION);

        verify(mockedCOAPClientFactory).createClient(eq(TEST_CONFIGURATION));
        verify(mockedCOAPClientFactory).createOptions(eq(TEST_CONFIGURATION));
    }

    @Test
    public void testReloadAndInit() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);

        when(mockedCOAPClientFactory.createClient(any(ConnectorConfiguration.class)))
                .thenReturn(mockedClient);
        when(mockedClient.getEndpoint()).thenReturn(mockedEndpoint);
        when(mockedCOAPClientFactory.createOptions(any(ConnectorConfiguration.class)))
                .thenReturn(mockedOptionSet);

        testConnector.loadAndInit(TEST_CONFIGURATION);

        verify(mockedEndpoint).destroy();
        verify(mockedClient).shutdown();
    }

    @Test
    public void testLoadAndInitCreateClientException() {
        when(mockedCOAPClientFactory.createClient(any(ConnectorConfiguration.class)))
                .thenThrow(new RuntimeException(""));

        assertThrows(RuntimeException.class, () -> testConnector.loadAndInit(TEST_CONFIGURATION));
    }

    @Test
    public void testUplink() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testConnector, OPTION_SET_FIELD_NAME, mockedOptionSet);

        try (MockedStatic<Request> requestStatic = mockStatic(Request.class)) {
            requestStatic.when(Request::newPost).thenReturn(mockedRequest);
            when(mockedOptionSet.setContentFormat(anyInt())).thenReturn(mockedOptionSet);
            when(mockedRequest.setPayload(any(byte[].class))).thenReturn(mockedRequest);
            when(mockedRequest.setOptions(any(OptionSet.class))).thenReturn(mockedRequest);
            when(mockedClient.advanced(any(Request.class))).thenReturn(mockedResponse);
            when(mockedResponse.getCode()).thenReturn(CoAP.ResponseCode.CREATED);

            testConnector.uplink(TEST_PAYLOAD);

            verify(mockedOptionSet).setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
            verify(mockedRequest).setPayload(eq(TEST_PAYLOAD));
            verify(mockedRequest).setOptions(eq(mockedOptionSet));
            verify(mockedClient).advanced(eq(mockedRequest));
            verify(mockedResponse).getCode();
        }
    }

    @Test
    public void testUplinkCborMessage() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testConnector, OPTION_SET_FIELD_NAME, mockedOptionSet);

        try (MockedStatic<Request> requestStatic = mockStatic(Request.class)) {
            requestStatic.when(Request::newPost).thenReturn(mockedRequest);
            when(mockedOptionSet.setContentFormat(anyInt())).thenReturn(mockedOptionSet);
            when(mockedRequest.setPayload(any(byte[].class))).thenReturn(mockedRequest);
            when(mockedRequest.setOptions(any(OptionSet.class))).thenReturn(mockedRequest);
            when(mockedClient.advanced(any(Request.class))).thenReturn(mockedResponse);
            when(mockedResponse.getCode()).thenReturn(CoAP.ResponseCode.CREATED);

            testConnector.uplink(TEST_PAYLOAD, ContentType.CBOR);

            verify(mockedOptionSet).setContentFormat(MediaTypeRegistry.APPLICATION_CBOR);
            verify(mockedRequest).setPayload(eq(TEST_PAYLOAD));
            verify(mockedRequest).setOptions(eq(mockedOptionSet));
            verify(mockedClient).advanced(eq(mockedRequest));
            verify(mockedResponse).getCode();
        }
    }

    @Test
    public void testUplinkMessagePackMessage() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testConnector, OPTION_SET_FIELD_NAME, mockedOptionSet);

        try (MockedStatic<Request> requestStatic = mockStatic(Request.class)) {
            requestStatic.when(Request::newPost).thenReturn(mockedRequest);
            when(mockedOptionSet.setContentFormat(anyInt())).thenReturn(mockedOptionSet);
            when(mockedRequest.setPayload(any(byte[].class))).thenReturn(mockedRequest);
            when(mockedRequest.setOptions(any(OptionSet.class))).thenReturn(mockedRequest);
            when(mockedClient.advanced(any(Request.class))).thenReturn(mockedResponse);
            when(mockedResponse.getCode()).thenReturn(CoAP.ResponseCode.CREATED);

            testConnector.uplink(TEST_PAYLOAD, ContentType.MESSAGE_PACK);

            verify(mockedOptionSet).setContentFormat(UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE);
            verify(mockedRequest).setPayload(eq(TEST_PAYLOAD));
            verify(mockedRequest).setOptions(eq(mockedOptionSet));
            verify(mockedClient).advanced(eq(mockedRequest));
            verify(mockedResponse).getCode();
        }
    }

    @Test
    public void testUplinkNoClient() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, (Object) null);

        testConnector.uplink(TEST_PAYLOAD);

        assertTrue(true, NO_EXCEPTION_THROWN_MESSAGE);
    }
    @Test
    public void testUplinkNoResponse() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testConnector, OPTION_SET_FIELD_NAME, mockedOptionSet);

        try (MockedStatic<Request> requestStatic = mockStatic(Request.class)) {
            requestStatic.when(Request::newPost).thenReturn(mockedRequest);
            when(mockedRequest.setPayload(any(byte[].class))).thenReturn(mockedRequest);
            when(mockedRequest.setOptions(any(OptionSet.class))).thenReturn(mockedRequest);
            when(mockedClient.advanced(any(Request.class))).thenReturn(null);

            testConnector.uplink(TEST_PAYLOAD);

            assertTrue(true, NO_EXCEPTION_THROWN_MESSAGE);
        }
    }

    @Test
    public void testUplinkErrorResponse() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testConnector, OPTION_SET_FIELD_NAME, mockedOptionSet);

        try (MockedStatic<Request> requestStatic = mockStatic(Request.class)) {
            requestStatic.when(Request::newPost).thenReturn(mockedRequest);
            when(mockedRequest.setPayload(any(byte[].class))).thenReturn(mockedRequest);
            when(mockedRequest.setOptions(any(OptionSet.class))).thenReturn(mockedRequest);
            when(mockedClient.advanced(any(Request.class))).thenReturn(mockedResponse);
            when(mockedResponse.getCode()).thenReturn(CoAP.ResponseCode.BAD_REQUEST);

            testConnector.uplink(TEST_PAYLOAD);

            assertTrue(true, NO_EXCEPTION_THROWN_MESSAGE);
        }
    }

    @Test
    public void testUplinkExceptionCaught() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testConnector, OPTION_SET_FIELD_NAME, mockedOptionSet);

        try (MockedStatic<Request> requestStatic = mockStatic(Request.class)) {
            requestStatic.when(Request::newPost).thenReturn(mockedRequest);
            when(mockedRequest.setPayload(any(byte[].class))).thenReturn(mockedRequest);
            when(mockedRequest.setOptions(any(OptionSet.class))).thenReturn(mockedRequest);
            when(mockedClient.advanced(any(Request.class))).thenThrow(new RuntimeException(""));

            testConnector.uplink(TEST_PAYLOAD);

            assertTrue(true, NO_EXCEPTION_THROWN_MESSAGE);
        }
    }

    @Test
    public void testIsConnected() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);

        when(mockedClient.ping()).thenReturn(true);

        boolean connected = testConnector.isConnected();

        assertTrue(connected);
    }

    @Test
    public void testIsConnectedNoClient() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, (Object) null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedNoPing() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);

        when(mockedClient.ping()).thenReturn(false);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);

        when(mockedClient.getEndpoint()).thenReturn(mockedEndpoint);

        testConnector.close();

        verify(mockedEndpoint).destroy();
        verify(mockedClient).shutdown();
    }

    @Test
    public void testCloseWithoutCOAPClient() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, (Object) null);

        testConnector.close();

        assertTrue(true, NO_EXCEPTION_THROWN_MESSAGE);
    }

    @Test
    public void testCloseWithoutEndpoint() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedClient);

        when(mockedClient.getEndpoint()).thenReturn(null);

        testConnector.close();

        assertTrue(true, NO_EXCEPTION_THROWN_MESSAGE);
    }
}
