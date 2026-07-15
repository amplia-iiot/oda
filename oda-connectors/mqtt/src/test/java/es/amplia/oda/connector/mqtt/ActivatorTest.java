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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
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
        List<List<?>> factoryArgs = new ArrayList<>();
        List<List<?>> dispatcherArgs = new ArrayList<>();
        List<List<?>> connectorArgs = new ArrayList<>();
        List<List<?>> deviceInfoProviderArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        List<List<?>> serviceListenerArgs = new ArrayList<>();

        try (MockedConstruction<MqttClientFactoryProxy> factoryCons =
                     mockConstruction(MqttClientFactoryProxy.class,
                             (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DispatcherProxy> dispatcherCons =
                     mockConstruction(DispatcherProxy.class,
                             (mock, mctx) -> dispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MqttConnector> connectorCons =
                     mockConstruction(MqttConnector.class,
                             (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DeviceInfoProviderProxy> deviceInfoProviderCons =
                     mockConstruction(DeviceInfoProviderProxy.class,
                             (mock, mctx) -> deviceInfoProviderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurationUpdateHandlerImpl> configHandlerCons =
                     mockConstruction(ConfigurationUpdateHandlerImpl.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> serviceListenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, factoryCons.constructed().size());
            assertEquals(mockedContext, factoryArgs.get(0).get(0));
            assertEquals(1, dispatcherCons.constructed().size());
            assertEquals(mockedContext, dispatcherArgs.get(0).get(0));
            assertEquals(1, connectorCons.constructed().size());
            assertEquals(factoryCons.constructed().get(0), connectorArgs.get(0).get(0));
            assertEquals(dispatcherCons.constructed().get(0), connectorArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(OpenGateConnector.class),
                    eq(connectorCons.constructed().get(0)), any());
            assertEquals(1, deviceInfoProviderCons.constructed().size());
            assertEquals(mockedContext, deviceInfoProviderArgs.get(0).get(0));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(connectorCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(deviceInfoProviderCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(2, serviceListenerCons.constructed().size());
            assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
            assertEquals(MqttClientFactory.class, serviceListenerArgs.get(0).get(1));
            assertTrue(serviceListenerArgs.get(0).get(2) instanceof Runnable);
            assertEquals(mockedContext, serviceListenerArgs.get(1).get(0));
            assertEquals(DeviceInfoProvider.class, serviceListenerArgs.get(1).get(1));
            assertTrue(serviceListenerArgs.get(1).get(2) instanceof Runnable);
        }
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
