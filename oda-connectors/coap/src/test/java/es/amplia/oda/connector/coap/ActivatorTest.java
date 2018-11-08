package es.amplia.oda.connector.coap;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atserver.api.ATManagerProxy;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.connector.coap.configuration.ConfigurationUpdateHandlerImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private ATManagerProxy mockedATManager;
    @Mock
    private COAPClientFactory mockedCOAPClientFactory;
    @Mock
    private COAPConnector mockedConnector;
    @Mock
    private ConfigurationUpdateHandlerImpl mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceListenerBundle<DeviceInfoProvider> mockedDeviceInfoServiceListener;
    @Mock
    private ServiceListenerBundle<ATManager> mockedATManagerServiceListener;
    @Mock
    private ServiceRegistration<OpenGateConnector> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProvider);
        PowerMockito.whenNew(ATManagerProxy.class).withAnyArguments().thenReturn(mockedATManager);
        PowerMockito.whenNew(COAPClientFactory.class).withAnyArguments().thenReturn(mockedCOAPClientFactory);
        PowerMockito.whenNew(COAPConnector.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(ConfigurationUpdateHandlerImpl.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class)
                .withArguments(any(BundleContext.class), eq(DeviceInfoProvider.class), any())
                .thenReturn(mockedDeviceInfoServiceListener);
        PowerMockito.whenNew(ServiceListenerBundle.class)
                .withArguments(any(BundleContext.class), eq(ATManager.class), any())
                .thenReturn(mockedATManagerServiceListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(DeviceInfoProviderProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ATManagerProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(COAPClientFactory.class).withArguments(eq(mockedDeviceInfoProvider), eq(mockedATManager));
        PowerMockito.verifyNew(COAPConnector.class).withArguments(eq(mockedCOAPClientFactory));
        PowerMockito.verifyNew(ConfigurationUpdateHandlerImpl.class).withArguments(eq(mockedConnector));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any());
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(ATManager.class), any());
        verify(mockedContext).registerService(eq(OpenGateConnector.class), eq(mockedConnector), any());
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        doThrow(new RuntimeException("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "atManager", mockedATManager);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "deviceInfoServiceListener", mockedDeviceInfoServiceListener);
        Whitebox.setInternalState(testActivator, "atManagerServiceListener", mockedATManagerServiceListener);
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedDeviceInfoServiceListener).close();
        verify(mockedATManagerServiceListener).close();
        verify(mockedATManager).close();
        verify(mockedConfigBundle).close();
        verify(mockedConnector).close();
        verify(mockedDeviceInfoProvider).close();
    }
}