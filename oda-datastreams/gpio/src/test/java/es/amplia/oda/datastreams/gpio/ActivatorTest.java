package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.GpioServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.gpio.configuration.DatastreamsGpioConfigurationHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

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
    private GpioServiceProxy mockedGpioService;
    @Mock
    private EventPublisherProxy mockedEventPublisher;
    @Mock
    private GpioDatastreamsFactoryImpl mockedFactory;
    @Mock
    private ServiceRegistrationManagerOsgi<DatastreamsGetter> mockedGetterRegistrationManager;
    @Mock
    private ServiceRegistrationManagerOsgi<DatastreamsSetter> mockedSetterRegistrationManager;
    @Mock
    private GpioDatastreamsManager mockedManager;
    @Mock
    private DatastreamsGpioConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceListenerBundle<GpioService> mockedGpioServiceListener;
    @Mock
    private ServiceListenerBundle<DeviceInfoProvider> mockedDeviceInfoProviderServiceListener;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(GpioServiceProxy.class).withAnyArguments().thenReturn(mockedGpioService);
        PowerMockito.whenNew(EventPublisherProxy.class).withAnyArguments().thenReturn(mockedEventPublisher);
        PowerMockito.whenNew(GpioDatastreamsFactoryImpl.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(any(BundleContext.class), eq(DatastreamsGetter.class))
                .thenReturn(mockedGetterRegistrationManager);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(any(BundleContext.class), eq(DatastreamsSetter.class))
                .thenReturn(mockedSetterRegistrationManager);
        PowerMockito.whenNew(GpioDatastreamsManager.class).withAnyArguments().thenReturn(mockedManager);
        PowerMockito.whenNew(DatastreamsGpioConfigurationHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class)
                .withArguments(any(BundleContext.class), eq(GpioService.class), any())
                .thenReturn(mockedGpioServiceListener);
        PowerMockito.whenNew(ServiceListenerBundle.class)
                .withArguments(any(BundleContext.class), eq(DeviceInfoProvider.class), any())
                .thenReturn(mockedDeviceInfoProviderServiceListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(GpioServiceProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(EventPublisherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(GpioDatastreamsFactoryImpl.class)
                .withArguments(eq(mockedGpioService), eq(mockedEventPublisher));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(GpioDatastreamsManager.class)
                .withArguments(eq(mockedFactory), eq(mockedGetterRegistrationManager), eq(mockedSetterRegistrationManager));
        PowerMockito.verifyNew(DatastreamsGpioConfigurationHandler.class)
                .withArguments(eq(mockedManager), eq(mockedGpioService));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(GpioService.class), any(Runnable.class));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any(Runnable.class));
    }

    @Test
    public void testOnServiceChanged() {
        testActivator.onServiceChanged(mockedConfigHandler);

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionCaught() {
        doThrow(new RuntimeException("")).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged(mockedConfigHandler);

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "gpioService", mockedGpioService);
        Whitebox.setInternalState(testActivator, "eventPublisher", mockedEventPublisher);
        Whitebox.setInternalState(testActivator, "manager", mockedManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "gpioServiceListener", mockedGpioServiceListener);
        Whitebox.setInternalState(testActivator, "deviceInfoProviderServiceListener",
                mockedDeviceInfoProviderServiceListener);

        testActivator.stop(mockedContext);

        verify(mockedConfigurableBundle).close();
        verify(mockedManager).close();
        verify(mockedEventPublisher).close();
        verify(mockedGpioService).close();
        verify(mockedDeviceInfoProviderServiceListener).close();
    }
}