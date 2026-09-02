package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.ResponseDispatcherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;

import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfigurationUpdateHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private MqttClientFactoryProxy mockedMqttClientFactory;
    @Mock
    private SerializerProxy mockedSerializer;
    @Mock
    private EventPublisherProxy mockedEventPublisher;
    @Mock
    private MqttDatastreamsOrchestrator mockedOrchestrator;
    @Mock
    private MqttDatastreamsConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceRegistration<MqttDatastreamsService> mockedRegistration;
    @Mock
    private ServiceListenerBundle<MqttClientFactory> mockedListener;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private ResponseDispatcherProxy mockedResponseDispatcher;

    @Test
    public void testStart() throws Exception {
        List<List<?>> clientFactoryArgs = new ArrayList<>();
        List<List<?>> serializerArgs = new ArrayList<>();
        List<List<?>> eventPublisherArgs = new ArrayList<>();
        List<List<?>> orchestratorArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        List<List<?>> listenerArgs = new ArrayList<>();
        try (MockedConstruction<MqttClientFactoryProxy> clientFactoryCons =
                     mockConstruction(MqttClientFactoryProxy.class,
                             (mock, mctx) -> clientFactoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SerializerProxy> serializerCons =
                     mockConstruction(SerializerProxy.class,
                             (mock, mctx) -> serializerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DeviceInfoProviderProxy> deviceInfoProviderCons =
                     mockConstruction(DeviceInfoProviderProxy.class);
             MockedConstruction<ResponseDispatcherProxy> responseDispatcherCons =
                     mockConstruction(ResponseDispatcherProxy.class);
             MockedConstruction<EventPublisherProxy> eventPublisherCons =
                     mockConstruction(EventPublisherProxy.class,
                             (mock, mctx) -> eventPublisherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MqttDatastreamsOrchestrator> orchestratorCons =
                     mockConstruction(MqttDatastreamsOrchestrator.class,
                             (mock, mctx) -> orchestratorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MqttDatastreamsConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(MqttDatastreamsConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> listenerCons =
                     mockConstruction(ServiceListenerBundle.class,
                             (mock, mctx) -> listenerArgs.add(new ArrayList<>(mctx.arguments())))) {
            when(mockedContext.registerService(eq(MqttDatastreamsService.class), any(), any())).thenReturn(mockedRegistration);

            testActivator.start(mockedContext);

            assertEquals(1, clientFactoryCons.constructed().size());
            assertEquals(mockedContext, clientFactoryArgs.get(0).get(0));
            assertEquals(1, serializerCons.constructed().size());
            assertEquals(mockedContext, serializerArgs.get(0).get(0));
            assertEquals(ContentType.JSON, serializerArgs.get(0).get(1));
            assertEquals(1, eventPublisherCons.constructed().size());
            assertEquals(mockedContext, eventPublisherArgs.get(0).get(0));
            assertEquals(1, orchestratorCons.constructed().size());
            assertEquals(clientFactoryCons.constructed().get(0), orchestratorArgs.get(0).get(0));
            assertEquals(serializerCons.constructed().get(0), orchestratorArgs.get(0).get(1));
            assertEquals(eventPublisherCons.constructed().get(0), orchestratorArgs.get(0).get(2));
            assertEquals(deviceInfoProviderCons.constructed().get(0), orchestratorArgs.get(0).get(3));
            assertEquals(responseDispatcherCons.constructed().get(0), orchestratorArgs.get(0).get(4));
            assertEquals(mockedContext, orchestratorArgs.get(0).get(5));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(orchestratorCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            verify(mockedContext).registerService(eq(MqttDatastreamsService.class), any(), any());
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
            assertEquals(1, listenerCons.constructed().size());
            assertEquals(mockedContext, listenerArgs.get(0).get(0));
            assertEquals(MqttClientFactory.class, listenerArgs.get(0).get(1));
        }
    }

    @Test
    public void testOnServiceChanged() throws MqttException {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedExceptionIsCaught() throws MqttException {
        Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

        doThrow(new RuntimeException()).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "mqttClientFactory", mockedMqttClientFactory);
        Whitebox.setInternalState(testActivator, "serializer", mockedSerializer);
        Whitebox.setInternalState(testActivator, "eventPublisher", mockedEventPublisher);
        Whitebox.setInternalState(testActivator, "mqttDatastreamsOrchestrator", mockedOrchestrator);
        Whitebox.setInternalState(testActivator, "mqttDatastreamServiceRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "mqttClientFactoryListener", mockedListener);
        Whitebox.setInternalState(testActivator, "deviceInfoProvider", mockedDeviceInfoProvider);
        Whitebox.setInternalState(testActivator, "responseDispatcher", mockedResponseDispatcher);

        testActivator.stop(mockedContext);

        verify(mockedListener).close();
        verify(mockedConfigBundle).close();
        verify(mockedRegistration).unregister();
        verify(mockedOrchestrator).close();
        verify(mockedMqttClientFactory).close();
        verify(mockedSerializer).close();
        verify(mockedEventPublisher).close();
        verify(mockedDeviceInfoProvider).close();
        verify(mockedResponseDispatcher).close();
    }
}