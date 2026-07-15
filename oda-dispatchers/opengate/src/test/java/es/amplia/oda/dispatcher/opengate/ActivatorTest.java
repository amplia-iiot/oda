package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.ResponseDispatcher;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.OperationSenderProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.SerializerProviderOsgi;
import es.amplia.oda.core.commons.utils.SchedulerImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.dispatcher.opengate.event.EventDispatcherFactoryImpl;
import es.amplia.oda.dispatcher.opengate.operation.processor.OpenGateOperationProcessorFactoryImpl;
import es.amplia.oda.event.api.EventDispatcher;

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
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private SerializerProviderOsgi mockedSerializerProvider;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private OpenGateOperationProcessorFactoryImpl mockedFactory;
    @Mock
    private ServiceRegistration<Dispatcher> mockedDispatcherRegistration;
    @Mock
    private OpenGateConnectorProxy mockedConnector;
    @Mock
    private SchedulerImpl mockedScheduler;
    @Mock
    private ServiceRegistrationManagerOsgi<EventDispatcher> mockedEventDispatcherRegistrationManager;
    @Mock
    private ServiceRegistration<ResponseDispatcher> mockedResponseDispatcherRegistration;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private OperationProcessor mockedProcessor;
    @Mock
    private OperationSenderProxy mockedOperationSender;


    @Test
    public void testStart() throws Exception {
        List<List<?>> serializerProviderArgs = new ArrayList<>();
        List<List<?>> deviceInfoProviderArgs = new ArrayList<>();
        List<List<?>> factoryArgs = new ArrayList<>();
        List<List<?>> dispatcherArgs = new ArrayList<>();
        List<List<?>> connectorArgs = new ArrayList<>();
        List<List<?>> eventDispatcherFactoryArgs = new ArrayList<>();
        List<List<?>> schedulerArgs = new ArrayList<>();
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        try (MockedConstruction<SerializerProviderOsgi> serializerProviderCons =
                     mockConstruction(SerializerProviderOsgi.class,
                             (mock, mctx) -> serializerProviderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DeviceInfoProviderProxy> deviceInfoProviderCons =
                     mockConstruction(DeviceInfoProviderProxy.class,
                             (mock, mctx) -> deviceInfoProviderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationSenderProxy> operationSenderCons =
                     mockConstruction(OperationSenderProxy.class);
             MockedConstruction<OpenGateOperationProcessorFactoryImpl> factoryCons =
                     mockConstruction(OpenGateOperationProcessorFactoryImpl.class,
                             (mock, mctx) -> {
                                 factoryArgs.add(new ArrayList<>(mctx.arguments()));
                                 when(mock.createOperationProcessor()).thenReturn(mockedProcessor);
                             });
             MockedConstruction<OpenGateOperationDispatcher> dispatcherCons =
                     mockConstruction(OpenGateOperationDispatcher.class,
                             (mock, mctx) -> dispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OpenGateConnectorProxy> connectorCons =
                     mockConstruction(OpenGateConnectorProxy.class,
                             (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventDispatcherFactoryImpl> eventDispatcherFactoryCons =
                     mockConstruction(EventDispatcherFactoryImpl.class,
                             (mock, mctx) -> eventDispatcherFactoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SchedulerImpl> schedulerCons = mockConstruction(SchedulerImpl.class,
                     (mock, mctx) -> schedulerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DispatcherConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(DispatcherConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, serializerProviderCons.constructed().size());
            assertEquals(mockedContext, serializerProviderArgs.get(0).get(0));
            assertEquals(1, deviceInfoProviderCons.constructed().size());
            assertEquals(mockedContext, deviceInfoProviderArgs.get(0).get(0));
            assertEquals(1, factoryCons.constructed().size());
            assertEquals(mockedContext, factoryArgs.get(0).get(0));
            assertEquals(1, dispatcherCons.constructed().size());
            assertEquals(serializerProviderCons.constructed().get(0), dispatcherArgs.get(0).get(0));
            assertEquals(deviceInfoProviderCons.constructed().get(0), dispatcherArgs.get(0).get(1));
            assertEquals(mockedProcessor, dispatcherArgs.get(0).get(2));
            assertEquals(operationSenderCons.constructed().get(0), dispatcherArgs.get(0).get(3));
            assertEquals(1, connectorCons.constructed().size());
            assertEquals(mockedContext, connectorArgs.get(0).get(0));
            assertEquals(1, schedulerCons.constructed().size());
            assertTrue(schedulerArgs.get(0).get(0) instanceof ScheduledExecutorService);
            assertEquals(1, eventDispatcherFactoryCons.constructed().size());
            assertEquals(deviceInfoProviderCons.constructed().get(0), eventDispatcherFactoryArgs.get(0).get(0));
            assertEquals(serializerProviderCons.constructed().get(0), eventDispatcherFactoryArgs.get(0).get(1));
            assertEquals(connectorCons.constructed().get(0), eventDispatcherFactoryArgs.get(0).get(2));
            assertEquals(schedulerCons.constructed().get(0), eventDispatcherFactoryArgs.get(0).get(3));
            assertEquals(1, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(EventDispatcher.class, registrationManagerArgs.get(0).get(1));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(eventDispatcherFactoryCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(schedulerCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(registrationManagerCons.constructed().get(0), configHandlerArgs.get(0).get(2));
            assertEquals(dispatcherCons.constructed().get(0), configHandlerArgs.get(0).get(3));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(Dispatcher.class), eq(dispatcherCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "serializerProvider", mockedSerializerProvider);
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "opSender", mockedOperationSender);
        Whitebox.setInternalState(testActivator, "factory", mockedFactory);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "scheduler", mockedScheduler);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "operationDispatcherRegistration", mockedDispatcherRegistration);
        Whitebox.setInternalState(testActivator, "eventDispatcherServiceRegistrationManager",
                mockedEventDispatcherRegistrationManager);
        Whitebox.setInternalState(testActivator, "responseDispatcherRegistration",
                mockedResponseDispatcherRegistration);

        testActivator.stop(mockedContext);

        verify(mockedConfigBundle).close();
        verify(mockedEventDispatcherRegistrationManager).unregister();
        verify(mockedScheduler).close();
        verify(mockedConnector).close();
        verify(mockedDispatcherRegistration).unregister();
        verify(mockedSerializerProvider).close();
        verify(mockedDeviceInfoProvider).close();
        verify(mockedFactory).close();
        verify(mockedOperationSender).close();
        verify(mockedResponseDispatcherRegistration).unregister();
    }
}
