package es.amplia.oda.connector.websocket;

import es.amplia.oda.connector.websocket.configuration.WebSocketConfigurationUpdateHandler;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private DispatcherProxy mockedDispatcher;
    @Mock
    private WebSocketConnector mockedConnector;
    @Mock
    private WebSocketConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceRegistration<OpenGateConnector> mockedRegistration;
    @Mock
    private ServiceListenerBundle<DeviceInfoProvider> mockedListener;

    @Test
    public void testStart() throws Exception {
        List<List<?>> deviceInfoProviderArgs = new ArrayList<>();
        List<List<?>> dispatcherArgs = new ArrayList<>();
        List<List<?>> factoryArgs = new ArrayList<>();
        List<List<?>> connectorArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        List<List<?>> serviceListenerArgs = new ArrayList<>();

        try (MockedConstruction<DeviceInfoProviderProxy> deviceInfoProviderCons =
                     mockConstruction(DeviceInfoProviderProxy.class,
                             (mock, mctx) -> deviceInfoProviderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DispatcherProxy> dispatcherCons =
                     mockConstruction(DispatcherProxy.class,
                             (mock, mctx) -> dispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<WebSocketClientFactory> factoryCons =
                     mockConstruction(WebSocketClientFactory.class,
                             (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<WebSocketConnector> connectorCons =
                     mockConstruction(WebSocketConnector.class,
                             (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<WebSocketConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(WebSocketConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> serviceListenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, deviceInfoProviderCons.constructed().size());
            assertEquals(mockedContext, deviceInfoProviderArgs.get(0).get(0));
            assertEquals(1, dispatcherCons.constructed().size());
            assertEquals(mockedContext, dispatcherArgs.get(0).get(0));
            assertEquals(1, factoryCons.constructed().size());
            assertEquals(dispatcherCons.constructed().get(0), factoryArgs.get(0).get(0));
            assertEquals(1, connectorCons.constructed().size());
            assertEquals(deviceInfoProviderCons.constructed().get(0), connectorArgs.get(0).get(0));
            assertEquals(factoryCons.constructed().get(0), connectorArgs.get(0).get(1));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(connectorCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(OpenGateConnector.class),
                    eq(connectorCons.constructed().get(0)), any());
            assertEquals(1, serviceListenerCons.constructed().size());
            assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
            assertEquals(DeviceInfoProvider.class, serviceListenerArgs.get(0).get(1));
            assertTrue(serviceListenerArgs.get(0).get(2) instanceof Runnable);
        }
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, "webSocketConfigHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() {
        Whitebox.setInternalState(testActivator, "webSocketConfigHandler", mockedConfigHandler);

        doThrow(new ConfigurationException("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "dispatcher", mockedDispatcher);
        Whitebox.setInternalState(testActivator, "webSocketConnector", mockedConnector);
        Whitebox.setInternalState(testActivator, "webSocketConfigHandler", mockedConfigHandler);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "webSocketConnectorRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "deviceInfoProviderListener", mockedListener);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedListener).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedConnector).close();
        verify(mockedDispatcher).close();
        verify(mockedDeviceInfoProvider).close();
    }
}
