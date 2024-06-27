package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttException;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttPahoClientFactory.class)
public class MqttPahoClientFactoryTest {

    private static final String TEST_SERVER_URI = "tcp://testhost.server";
    private static final String TEST_CLIENT_ID = "testClient";

    private final MqttPahoClientFactory testFactory = new MqttPahoClientFactory();

    @Mock
    private org.eclipse.paho.client.mqttv3.MqttAsyncClient mockedInnerClient;
    @Mock
    private ResubscribeTopicsOnReconnectCallback mockedCallback;
    @Mock
    private MqttPahoClient mockedClient;

    @Test
    public void testCreateMqttClient() throws Exception {
        PowerMockito.whenNew(org.eclipse.paho.client.mqttv3.MqttAsyncClient.class).withAnyArguments().
                thenReturn(mockedInnerClient);
        PowerMockito.whenNew(ResubscribeTopicsOnReconnectCallback.class).withAnyArguments().thenReturn(mockedCallback);
        PowerMockito.whenNew(MqttPahoClient.class).withAnyArguments().thenReturn(mockedClient);

        testFactory.createMqttClient(TEST_SERVER_URI, TEST_CLIENT_ID);

        PowerMockito.verifyNew(org.eclipse.paho.client.mqttv3.MqttAsyncClient.class)
                .withArguments(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID), any(MemoryPersistence.class));
        PowerMockito.verifyNew(ResubscribeTopicsOnReconnectCallback.class).withNoArguments();
        PowerMockito.verifyNew(MqttPahoClient.class).withArguments(eq(mockedInnerClient), eq(mockedCallback));
    }

    @Test(expected = MqttException.class)
    public void testCreateMqttClientThrowsMqttException() throws Exception {
        PowerMockito.whenNew(org.eclipse.paho.client.mqttv3.MqttAsyncClient.class).withAnyArguments().
                thenThrow(new org.eclipse.paho.client.mqttv3.MqttException(1));

        testFactory.createMqttClient(TEST_SERVER_URI, TEST_CLIENT_ID);
    }
}