package es.amplia.oda.connector.http;

import es.amplia.oda.comms.http.HttpClientFactoryImpl;
import es.amplia.oda.connector.http.configuration.HttpConnectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private HttpClientFactoryImpl mockedHttpClientFactory;
    @Mock
    private HttpConnector mockedConnector;
    @Mock
    private HttpConnectorConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceRegistration<OpenGateConnector> mockedRegistration;
    @Mock
    private ServiceListenerBundle<DeviceInfoProvider> mockedListener;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProvider);
        PowerMockito.whenNew(HttpConnector.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(HttpConnectorConfigurationUpdateHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);
        PowerMockito.whenNew(HttpClientFactoryImpl.class).withNoArguments().thenReturn(mockedHttpClientFactory);


        testActivator.start(mockedContext);

        PowerMockito.verifyNew(DeviceInfoProviderProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(HttpConnector.class).withArguments(eq(mockedDeviceInfoProvider), eq(mockedHttpClientFactory));
        PowerMockito.verifyNew(HttpConnectorConfigurationUpdateHandler.class).withArguments(eq(mockedConnector));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(OpenGateConnector.class), eq(mockedConnector), any());
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any(Runnable.class));
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, "httpConfigHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() {
        Whitebox.setInternalState(testActivator, "httpConfigHandler", mockedConfigHandler);

        doThrow(new RuntimeException()).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
        assertTrue("Exception is caught", true);
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "deviceInfoProviderListener", mockedListener);
        Whitebox.setInternalState(testActivator, "httpConnectorRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedListener).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedDeviceInfoProvider).close();
    }
}