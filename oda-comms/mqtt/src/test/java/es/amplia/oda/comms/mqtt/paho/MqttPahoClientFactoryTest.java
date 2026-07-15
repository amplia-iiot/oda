package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttException;

import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MqttPahoClientFactoryTest {

    private static final String TEST_SERVER_URI = "tcp://testhost.server";
    private static final String TEST_CLIENT_ID = "testClient";

    private final MqttPahoClientFactory testFactory = new MqttPahoClientFactory();

    @Test
    public void testCreateMqttClient() throws Exception {
        List<List<?>> innerClientArgs = new ArrayList<>();
        List<List<?>> clientArgs = new ArrayList<>();

        try (MockedConstruction<org.eclipse.paho.client.mqttv3.MqttAsyncClient> innerClientCons =
                     mockConstruction(org.eclipse.paho.client.mqttv3.MqttAsyncClient.class,
                             (mock, mctx) -> innerClientArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ResubscribeTopicsOnReconnectCallback> callbackCons =
                     mockConstruction(ResubscribeTopicsOnReconnectCallback.class);
             MockedConstruction<MqttPahoClient> clientCons =
                     mockConstruction(MqttPahoClient.class,
                             (mock, mctx) -> clientArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createMqttClient(TEST_SERVER_URI, TEST_CLIENT_ID);

            assertEquals(1, innerClientCons.constructed().size());
            assertEquals(TEST_SERVER_URI, innerClientArgs.get(0).get(0));
            assertEquals(TEST_CLIENT_ID, innerClientArgs.get(0).get(1));
            assertTrue(innerClientArgs.get(0).get(2) instanceof MemoryPersistence);
            assertEquals(1, callbackCons.constructed().size());
            assertEquals(1, clientCons.constructed().size());
            assertEquals(innerClientCons.constructed().get(0), clientArgs.get(0).get(0));
            assertEquals(callbackCons.constructed().get(0), clientArgs.get(0).get(1));
        }
    }

    @Test(expected = MqttException.class)
    public void testCreateMqttClientThrowsMqttException() throws Exception {
        try (MockedConstruction<MemoryPersistence> persistenceCons =
                     mockConstruction(MemoryPersistence.class,
                             (mock, mctx) -> doThrow(new org.eclipse.paho.client.mqttv3.MqttPersistenceException(1))
                                     .when(mock).open(anyString(), anyString()))) {

            testFactory.createMqttClient(TEST_SERVER_URI, TEST_CLIENT_ID);
        }
    }
}
