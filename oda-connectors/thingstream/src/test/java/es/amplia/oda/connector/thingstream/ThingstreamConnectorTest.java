package es.amplia.oda.connector.thingstream;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration;

import com.myriadgroup.iot.sdk.IoTSDKConstants;
import com.myriadgroup.iot.sdk.client.message.IMessageClient;
import com.myriadgroup.iot.sdk.client.message.MessageClientException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.UnsupportedEncodingException;

import static es.amplia.oda.connector.thingstream.ThingstreamConnector.MESSAGE_LIMIT;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MessageClientFactory.class)
public class ThingstreamConnectorTest {

    private static final String TOPIC = "test/topic";
    private static final String OPERATION_TOPIC = "test/operation/topic";
    private static final ConnectorConfiguration.QOS QOS = ConnectorConfiguration.QOS.QOS_0;
    private static final String CLIENT_ID = "clientId";
    private static final int SHORT_CODE = 123;
    private static final String MESSAGE = "This is a test message to uplink";

    private static final String MESSAGE_CLIENT_FIELD_NAME = "messageClient";
    private static final String DATA_TOPIC_FIELD_NAME = "dataTopic";
    private static final String OPERATION_TOPIC_FIELD_NAME = "operationTopic";
    private static final String QOS_FIELD_NAME = "qos";
    private static final String NO_EXCEPTION_MESSAGE = "No exception is thrown";

    @Mock
    private ATManager mockedAtManager;
    @Mock
    private Dispatcher mockedDispatcher;
    @InjectMocks
    private ThingstreamConnector testConnector;

    @Mock
    private IMessageClient mockedMessageClient;

    @Test
    public void testLoadConfigurationAndInit() throws MessageClientException {
        ConnectorConfiguration configuration =
                ConnectorConfiguration.builder()
                        .dataTopic(TOPIC)
                        .operationTopic(OPERATION_TOPIC)
                        .qos(QOS)
                        .clientType(ConnectorConfiguration.ClientType.MQTTSN)
                        .clientId(CLIENT_ID)
                        .shortCode(SHORT_CODE)
                        .build();

        PowerMockito.mockStatic(MessageClientFactory.class);
        PowerMockito.when(MessageClientFactory.createMessageClient(any(ConnectorConfiguration.class), any(ATManager.class)))
            .thenReturn(mockedMessageClient);

        testConnector.loadConfigurationAndInit(configuration);

        PowerMockito.verifyStatic(MessageClientFactory.class);
        MessageClientFactory.createMessageClient(eq(configuration), eq(mockedAtManager));
        verify(mockedMessageClient).create();
        verify(mockedMessageClient).connect(eq(true), eq(testConnector));
        verify(mockedMessageClient).subscribe(eq(TOPIC), eq(IMessageClient.QOS.QOS0));
        verify(mockedMessageClient).subscribe(eq(OPERATION_TOPIC), eq(IMessageClient.QOS.QOS0));
    }

    @Test
    public void testReloadConfigurationAndInit() throws MessageClientException {
        ConnectorConfiguration configuration =
                ConnectorConfiguration.builder()
                        .dataTopic(TOPIC)
                        .operationTopic(OPERATION_TOPIC)
                        .qos(QOS)
                        .clientType(ConnectorConfiguration.ClientType.MQTTSN)
                        .clientId(CLIENT_ID)
                        .shortCode(SHORT_CODE)
                        .build();

        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        PowerMockito.mockStatic(MessageClientFactory.class);
        PowerMockito.when(MessageClientFactory.createMessageClient(any(ConnectorConfiguration.class), any(ATManager.class)))
                .thenReturn(mockedMessageClient);

        testConnector.loadConfigurationAndInit(configuration);

        verify(mockedMessageClient).disconnect();
        verify(mockedMessageClient).destroy();
    }

    @Test(expected = MessageClientException.class)
    public void testLoadConfigurationAndInitException() throws MessageClientException {
        ConnectorConfiguration configuration =
                ConnectorConfiguration.builder()
                        .dataTopic(TOPIC)
                        .operationTopic(OPERATION_TOPIC)
                        .qos(QOS)
                        .clientType(ConnectorConfiguration.ClientType.MQTTSN)
                        .clientId(CLIENT_ID)
                        .shortCode(SHORT_CODE)
                        .build();

        PowerMockito.mockStatic(MessageClientFactory.class);
        PowerMockito.when(MessageClientFactory.createMessageClient(any(ConnectorConfiguration.class), any(ATManager.class)))
                .thenReturn(mockedMessageClient);

        doThrow(new MessageClientException("")).when(mockedMessageClient).create();

        testConnector.loadConfigurationAndInit(configuration);

        fail("Message client exception must be thrown");
    }

    @Test
    public void testLoadConfigurationAndInitQOS1() throws MessageClientException {
        ConnectorConfiguration configurationQos1 =
                ConnectorConfiguration.builder()
                        .dataTopic(TOPIC)
                        .operationTopic(OPERATION_TOPIC)
                        .qos(ConnectorConfiguration.QOS.QOS_1)
                        .clientType(ConnectorConfiguration.ClientType.MQTTSN)
                        .clientId(CLIENT_ID)
                        .shortCode(SHORT_CODE)
                        .build();

        PowerMockito.mockStatic(MessageClientFactory.class);
        PowerMockito.when(MessageClientFactory.createMessageClient(any(ConnectorConfiguration.class), any(ATManager.class)))
                .thenReturn(mockedMessageClient);

        testConnector.loadConfigurationAndInit(configurationQos1);

        verify(mockedMessageClient).subscribe(eq(TOPIC), eq(IMessageClient.QOS.QOS1));
        verify(mockedMessageClient).subscribe(eq(OPERATION_TOPIC), eq(IMessageClient.QOS.QOS1));
    }

    @Test
    public void testLoadConfigurationAndInitQOS2() throws MessageClientException {
        ConnectorConfiguration configurationQos2 =
                ConnectorConfiguration.builder()
                        .dataTopic(TOPIC)
                        .operationTopic(OPERATION_TOPIC)
                        .qos(ConnectorConfiguration.QOS.QOS_2)
                        .clientType(ConnectorConfiguration.ClientType.MQTTSN)
                        .clientId(CLIENT_ID)
                        .shortCode(SHORT_CODE)
                        .build();

        PowerMockito.mockStatic(MessageClientFactory.class);
        PowerMockito.when(MessageClientFactory.createMessageClient(any(ConnectorConfiguration.class), any(ATManager.class)))
                .thenReturn(mockedMessageClient);

        testConnector.loadConfigurationAndInit(configurationQos2);

        verify(mockedMessageClient).subscribe(eq(TOPIC), eq(IMessageClient.QOS.QOS2));
        verify(mockedMessageClient).subscribe(eq(OPERATION_TOPIC), eq(IMessageClient.QOS.QOS2));
    }

    @Test
    public void testUplink() throws MessageClientException, UnsupportedEncodingException {
        byte[] payload = MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING);

        Whitebox.setInternalState(testConnector, DATA_TOPIC_FIELD_NAME, TOPIC);
        Whitebox.setInternalState(testConnector, QOS_FIELD_NAME, IMessageClient.QOS.QOS0);
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenReturn(true);

        testConnector.uplink(payload);

        verify(mockedMessageClient).publish(eq(TOPIC), eq(IMessageClient.QOS.QOS0), aryEq(payload));
    }

    @Test
    public void testUplinkNoMessageClient() throws UnsupportedEncodingException {
        byte[] payload = MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING);

        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, null);

        testConnector.uplink(payload);

        assertTrue(NO_EXCEPTION_MESSAGE, true);
    }

    @Test
    public void testUplinkMessageClientNoConnected() throws MessageClientException, UnsupportedEncodingException {
        byte[] payload = MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING);

        Whitebox.setInternalState(testConnector, DATA_TOPIC_FIELD_NAME, TOPIC);
        Whitebox.setInternalState(testConnector, QOS_FIELD_NAME, IMessageClient.QOS.QOS0);
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenReturn(false);

        testConnector.uplink(payload);

        assertTrue(NO_EXCEPTION_MESSAGE, true);
        verify(mockedMessageClient, never()).publish(anyString(), any(IMessageClient.QOS.class), any());
    }

    @Test
    public void testUplinkMessageLimitExceeded() throws MessageClientException {
        byte[] payload = new byte[MESSAGE_LIMIT + 1];

        Whitebox.setInternalState(testConnector, DATA_TOPIC_FIELD_NAME, TOPIC);
        Whitebox.setInternalState(testConnector, QOS_FIELD_NAME, IMessageClient.QOS.QOS0);
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenReturn(true);

        testConnector.uplink(payload);

        assertTrue(NO_EXCEPTION_MESSAGE, true);
        verify(mockedMessageClient, never()).publish(anyString(), any(IMessageClient.QOS.class), any());
    }

    @Test
    public void testUplinkMessageExceptionCaught() throws MessageClientException, UnsupportedEncodingException {
        byte[] payload = MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING);

        Whitebox.setInternalState(testConnector, DATA_TOPIC_FIELD_NAME, TOPIC);
        Whitebox.setInternalState(testConnector, QOS_FIELD_NAME, IMessageClient.QOS.QOS0);
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenReturn(true);
        doThrow(new MessageClientException(""))
                .when(mockedMessageClient).publish(anyString(), any(IMessageClient.QOS.class), any());

        testConnector.uplink(payload);

        assertTrue("Exception is caught", true);
        verify(mockedMessageClient).publish(eq(TOPIC), eq(IMessageClient.QOS.QOS0), aryEq(payload));
    }

    @Test
    public void testIsConnected() throws MessageClientException {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenReturn(true);

        boolean connected = testConnector.isConnected();

        assertTrue(connected);
    }

    @Test
    public void testIsConnectedNullClient() {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedNoConnection() throws MessageClientException {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenReturn(false);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedExceptionCaught() throws MessageClientException {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);

        when(mockedMessageClient.isConnected()).thenThrow(new MessageClientException(""));

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testOnReceive() throws UnsupportedEncodingException {
        byte[] payload = MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING);

        Whitebox.setInternalState(testConnector, OPERATION_TOPIC_FIELD_NAME, OPERATION_TOPIC);

        testConnector.onReceive(OPERATION_TOPIC, payload, 0);

        verify(mockedDispatcher).process(aryEq(payload));
    }

    @Test
    public void testOnReceiveNoOperationTopic() throws UnsupportedEncodingException {
        byte[] payload = MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING);

        Whitebox.setInternalState(testConnector, OPERATION_TOPIC_FIELD_NAME, OPERATION_TOPIC);

        testConnector.onReceive(TOPIC, payload, 0);

        verify(mockedDispatcher, never()).process(any());
    }

    @Test
    public void testClose() throws MessageClientException {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);
        Whitebox.setInternalState(testConnector, DATA_TOPIC_FIELD_NAME, TOPIC);
        Whitebox.setInternalState(testConnector, OPERATION_TOPIC_FIELD_NAME, OPERATION_TOPIC);

        testConnector.close();

        verify(mockedMessageClient).unsubscribe(eq(TOPIC));
        verify(mockedMessageClient).unsubscribe(eq(OPERATION_TOPIC));
        verify(mockedMessageClient).disconnect();
        verify(mockedMessageClient).destroy();
    }

    @Test
    public void testCloseNoMessageClient() {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, null);

        testConnector.close();

        assertTrue(NO_EXCEPTION_MESSAGE, true);
    }

    @Test
    public void testCloseExceptionIsCaught() throws MessageClientException {
        Whitebox.setInternalState(testConnector, MESSAGE_CLIENT_FIELD_NAME, mockedMessageClient);
        Whitebox.setInternalState(testConnector, DATA_TOPIC_FIELD_NAME, TOPIC);
        Whitebox.setInternalState(testConnector, OPERATION_TOPIC_FIELD_NAME, OPERATION_TOPIC);

        doThrow(new MessageClientException("")).when(mockedMessageClient).destroy();

        testConnector.close();

        assertTrue("Exception is caught", true);
        verify(mockedMessageClient).disconnect();
        verify(mockedMessageClient).destroy();
    }
}