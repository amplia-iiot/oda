package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttPahoClientFactory.class)
public class MqttPahoClientFactoryTest {

    private static final String TEST_SERVER_URI = "test.host.server";
    private static final String TEST_CLIENT_ID = "testClient";

    private final MqttPahoClientFactory testFactory = new MqttPahoClientFactory();

    @Mock
    private org.eclipse.paho.client.mqttv3.MqttClient mockedInnerClient;

    @Test
    public void testCreateMqttClient() throws Exception {
        PowerMockito.whenNew(org.eclipse.paho.client.mqttv3.MqttClient.class).withAnyArguments().
                thenReturn(mockedInnerClient);

        MqttClient client = testFactory.createMqttClient(TEST_SERVER_URI, TEST_CLIENT_ID);

        PowerMockito.verifyNew(org.eclipse.paho.client.mqttv3.MqttClient.class)
                .withArguments(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID));
        assertEquals(mockedInnerClient, Whitebox.getInternalState(client, "innerClient"));
    }

    @Test(expected = MqttException.class)
    public void testCreateMqttClientThrowsMqttException() throws Exception {
        PowerMockito.whenNew(org.eclipse.paho.client.mqttv3.MqttClient.class).withAnyArguments().
                thenThrow(new org.eclipse.paho.client.mqttv3.MqttException(1));

        testFactory.createMqttClient(TEST_SERVER_URI, TEST_CLIENT_ID);
    }
}