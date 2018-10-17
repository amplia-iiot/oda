package es.amplia.oda.connector.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttConnectorTest {

    private static final String IOT_TOPIC = "iot/topic";
    private static final int QOS = 0;
    private static final boolean RETAINED = false;
    private static final String CLIENT_FIELD_NAME = "client";
    private static final String IOT_TOPIC_FIELD_NAME = "iotTopic";
    private static final String QOS_FIELD_NAME = "qos";
    private static final String RETAINED_FIELD_NAME = "retained";

    @InjectMocks
    private MqttConnector testConnector;

    @Mock
    private MqttClient mockedMqttClient;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedMqttClient);
        Whitebox.setInternalState(testConnector, IOT_TOPIC_FIELD_NAME, IOT_TOPIC);
        Whitebox.setInternalState(testConnector, QOS_FIELD_NAME, QOS);
        Whitebox.setInternalState(testConnector, RETAINED_FIELD_NAME, RETAINED);
    }

    @Test
    public void testUplink() throws MqttException {
        byte[] payload = new byte[]{1, 2, 3, 4};

        testConnector.uplink(payload);

        verify(mockedMqttClient).publish(eq(IOT_TOPIC), eq(payload), eq(QOS), eq(RETAINED));
    }

    @Test
    public void testUplinkMqttDisconnected() {
        byte[] payload = new byte[]{1, 2, 3, 4};

        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, null);

        testConnector.uplink(payload);

        verifyZeroInteractions(mockedMqttClient);
    }

    @Test
    public void testUplinkNullPayload() {
        testConnector.uplink(null);

        verifyZeroInteractions(mockedMqttClient);
    }

    @Test
    public void testUplinkMqttPublishExceptionCaught() throws MqttException {
        byte[] payload = new byte[]{1, 2, 3, 4};

        doThrow(new MqttException(0))
                .when(mockedMqttClient).publish(eq(IOT_TOPIC), eq(payload), eq(QOS), eq(RETAINED));

        testConnector.uplink(payload);

        verify(mockedMqttClient).publish(eq(IOT_TOPIC), eq(payload), eq(QOS), eq(RETAINED));
    }

    @Test
    public void testIsConnected() {
        when(mockedMqttClient.isConnected()).thenReturn(true);

        boolean connected = testConnector.isConnected();

        assertTrue(connected);
    }

    @Test
    public void testIsConnectedNullClient() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedClientNotConnected() {
        when(mockedMqttClient.isConnected()).thenReturn(false);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }
}