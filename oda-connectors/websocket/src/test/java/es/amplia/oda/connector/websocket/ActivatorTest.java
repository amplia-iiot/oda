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
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private DispatcherProxy mockedDispatcher;
    @Mock
    private WebSocketClientFactory mockedFactory;
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
        PowerMockito.whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProvider);
        PowerMockito.whenNew(DispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcher);
        PowerMockito.whenNew(WebSocketClientFactory.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(WebSocketConnector.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(WebSocketConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(DeviceInfoProviderProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(DispatcherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(WebSocketClientFactory.class).withArguments(eq(mockedDispatcher));
        PowerMockito.verifyNew(WebSocketConnector.class).withArguments(eq(mockedDeviceInfoProvider), eq(mockedFactory));
        PowerMockito.verifyNew(WebSocketConfigurationUpdateHandler.class).withArguments(eq(mockedConnector));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(OpenGateConnector.class), eq(mockedConnector), any());
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any(Runnable.class));
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