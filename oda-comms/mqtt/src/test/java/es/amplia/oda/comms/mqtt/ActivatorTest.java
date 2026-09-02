package es.amplia.oda.comms.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.paho.MqttPahoClientFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<MqttClientFactory> mockedRegistration;

    @Test
    public void testStart() {
        testActivator.start(mockedContext);

        verify(mockedContext).registerService(eq(MqttClientFactory.class), isA(MqttPahoClientFactory.class), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "mqttClientFactoryServiceRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
    }
}