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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private GpioServiceProxy mockedGpioService;
    @Mock
    private EventPublisherProxy mockedEventPublisher;
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
        List<List<?>> gpioServiceArgs = new ArrayList<>();
        List<List<?>> eventPublisherArgs = new ArrayList<>();
        List<List<?>> factoryArgs = new ArrayList<>();
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> managerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        List<List<?>> listenerArgs = new ArrayList<>();
        try (MockedConstruction<GpioServiceProxy> gpioServiceCons =
                     mockConstruction(GpioServiceProxy.class,
                             (mock, mctx) -> gpioServiceArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventPublisherProxy> eventPublisherCons =
                     mockConstruction(EventPublisherProxy.class,
                             (mock, mctx) -> eventPublisherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<GpioDatastreamsFactoryImpl> factoryCons =
                     mockConstruction(GpioDatastreamsFactoryImpl.class,
                             (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<GpioDatastreamsManager> managerCons =
                     mockConstruction(GpioDatastreamsManager.class,
                             (mock, mctx) -> managerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamsGpioConfigurationHandler> configHandlerCons =
                     mockConstruction(DatastreamsGpioConfigurationHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> listenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> listenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, gpioServiceCons.constructed().size());
            assertEquals(mockedContext, gpioServiceArgs.get(0).get(0));
            assertEquals(1, eventPublisherCons.constructed().size());
            assertEquals(mockedContext, eventPublisherArgs.get(0).get(0));
            assertEquals(1, factoryCons.constructed().size());
            assertEquals(gpioServiceCons.constructed().get(0), factoryArgs.get(0).get(0));
            assertEquals(eventPublisherCons.constructed().get(0), factoryArgs.get(0).get(1));
            assertEquals(2, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(DatastreamsGetter.class, registrationManagerArgs.get(0).get(1));
            assertEquals(mockedContext, registrationManagerArgs.get(1).get(0));
            assertEquals(DatastreamsSetter.class, registrationManagerArgs.get(1).get(1));
            assertEquals(1, managerCons.constructed().size());
            assertEquals(factoryCons.constructed().get(0), managerArgs.get(0).get(0));
            assertEquals(registrationManagerCons.constructed().get(0), managerArgs.get(0).get(1));
            assertEquals(registrationManagerCons.constructed().get(1), managerArgs.get(0).get(2));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(managerCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(gpioServiceCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(2, listenerCons.constructed().size());
            assertEquals(mockedContext, listenerArgs.get(0).get(0));
            assertEquals(GpioService.class, listenerArgs.get(0).get(1));
            assertTrue(listenerArgs.get(0).get(2) instanceof Runnable);
            assertEquals(mockedContext, listenerArgs.get(1).get(0));
            assertEquals(DeviceInfoProvider.class, listenerArgs.get(1).get(1));
            assertTrue(listenerArgs.get(1).get(2) instanceof Runnable);
        }
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