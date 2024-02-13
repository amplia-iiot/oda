package es.amplia.oda.connector.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.connector.mqtt.configuration.ConfigurationUpdateHandlerImpl;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private MqttClientFactoryProxy mockedFactory;
    @Mock
    private DispatcherProxy mockedDispatcher;
    @Mock
    private MqttConnector mockedConnector;
    @Mock
    private ServiceRegistration<OpenGateConnector> mockedRegistration;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private ConfigurationUpdateHandlerImpl mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceListenerBundle<?>  mockedListener;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(MqttClientFactoryProxy.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(DispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcher);
        PowerMockito.whenNew(MqttConnector.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProvider);
        PowerMockito.whenNew(ConfigurationUpdateHandlerImpl.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(MqttClientFactoryProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(DispatcherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(MqttConnector.class).withArguments(eq(mockedFactory), eq(mockedDispatcher));
        verify(mockedContext).registerService(eq(OpenGateConnector.class), eq(mockedConnector), any());
        PowerMockito.verifyNew(DeviceInfoProviderProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ConfigurationUpdateHandlerImpl.class)
                .withArguments(eq(mockedConnector), eq(mockedDeviceInfoProvider));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(MqttClientFactory.class), any(Runnable.class));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any(Runnable.class));
    }

    @Test
    public void testOnServiceChanged() throws MqttException {
        testActivator.onServiceChanged(mockedConfigHandler);

        verify(mockedConfigHandler).reapplyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() throws MqttException {
        doThrow(MqttException.class).when(mockedConfigHandler).reapplyConfiguration();

        testActivator.onServiceChanged(mockedConfigHandler);

        verify(mockedConfigHandler).reapplyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "openGateConnectorRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "mqttClientFactoryServiceListener", mockedListener);
        Whitebox.setInternalState(testActivator, "deviceInfoServiceListener", mockedListener);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "deviceIdProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "mqttClientFactory", mockedFactory);
        Whitebox.setInternalState(testActivator, "dispatcher", mockedDispatcher);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedListener, times(2)).close();
        verify(mockedConfigBundle).close();
        verify(mockedDeviceInfoProvider).close();
        verify(mockedConnector).close();
        verify(mockedDeviceInfoProvider).close();
        verify(mockedDispatcher).close();
    }
}