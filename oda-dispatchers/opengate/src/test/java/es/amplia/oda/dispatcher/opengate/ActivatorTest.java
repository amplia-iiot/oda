package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.Serializers;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.dispatcher.opengate.event.EventDispatcherFactoryImpl;
import es.amplia.oda.dispatcher.opengate.operation.processor.OpenGateOperationProcessorFactoryImpl;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private SerializerProxy mockedSerializer;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private OpenGateOperationProcessorFactoryImpl mockedFactory;
    @Mock
    private OpenGateOperationDispatcher mockedDispatcher;
    @Mock
    private ServiceRegistration<Dispatcher> mockedDispatcherRegistration;
    @Mock
    private OpenGateConnectorProxy mockedConnector;
    @Mock
    private EventDispatcherFactoryImpl mockedEventDispatcherFactory;
    @Mock
    private SchedulerImpl mockedScheduler;
    @Mock
    private ServiceRegistrationManagerOsgi<EventDispatcher> mockedEventDispatcherRegistrationManager;
    @Mock
    private DispatcherConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private OperationProcessor mockedProcessor;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
        PowerMockito.whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProvider);
        PowerMockito.whenNew(OpenGateOperationProcessorFactoryImpl.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(OpenGateOperationDispatcher.class).withAnyArguments().thenReturn(mockedDispatcher);
        PowerMockito.whenNew(OpenGateConnectorProxy.class).withAnyArguments().thenReturn(mockedConnector);
        PowerMockito.whenNew(EventDispatcherFactoryImpl.class).withAnyArguments()
                .thenReturn(mockedEventDispatcherFactory);
        PowerMockito.whenNew(SchedulerImpl.class).withAnyArguments().thenReturn(mockedScheduler);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
                .thenReturn(mockedEventDispatcherRegistrationManager);
        PowerMockito.whenNew(DispatcherConfigurationUpdateHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        when(mockedFactory.createOperationProcessor()).thenReturn(mockedProcessor);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(SerializerProxy.class)
                .withArguments(eq(mockedContext), eq(Serializers.SerializerType.JSON));
        PowerMockito.verifyNew(DeviceInfoProviderProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(OpenGateOperationProcessorFactoryImpl.class)
                .withArguments(eq(mockedContext), eq(mockedSerializer));
        PowerMockito.verifyNew(OpenGateOperationDispatcher.class)
                .withArguments(eq(mockedSerializer), eq(mockedDeviceInfoProvider), eq(mockedProcessor));
        PowerMockito.verifyNew(OpenGateConnectorProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(EventDispatcherFactoryImpl.class)
                .withArguments(eq(mockedDeviceInfoProvider), eq(mockedSerializer), eq(mockedConnector));
        PowerMockito.verifyNew(SchedulerImpl.class).withArguments(any(ScheduledExecutorService.class));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(EventDispatcher.class));
        PowerMockito.verifyNew(DispatcherConfigurationUpdateHandler.class)
                .withArguments(eq(mockedEventDispatcherFactory), eq(mockedScheduler),
                        eq(mockedEventDispatcherRegistrationManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(Dispatcher.class), eq(mockedDispatcher), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "serializer", mockedSerializer);
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "factory", mockedFactory);
        Whitebox.setInternalState(testActivator, "connector", mockedConnector);
        Whitebox.setInternalState(testActivator, "scheduler", mockedScheduler);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "operationDispatcherRegistration", mockedDispatcherRegistration);
        Whitebox.setInternalState(testActivator, "eventDispatcherServiceRegistrationManager",
                mockedEventDispatcherRegistrationManager);

        testActivator.stop(mockedContext);

        verify(mockedConfigBundle).close();
        verify(mockedEventDispatcherRegistrationManager).unregister();
        verify(mockedScheduler).close();
        verify(mockedConnector).close();
        verify(mockedDispatcherRegistration).unregister();
        verify(mockedSerializer).close();
        verify(mockedDeviceInfoProvider).close();
        verify(mockedFactory).close();
    }
}