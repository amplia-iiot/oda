package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NullConfigurationTest {
    @Test
    public void testConfigure() {
        MqttConfiguration nullConfiguration = new NullConfiguration();
        MqttConnectOptions mockedOptions = mock(MqttConnectOptions.class);

        nullConfiguration.configure(mockedOptions);

        verifyZeroInteractions(mockedOptions);
    }
}