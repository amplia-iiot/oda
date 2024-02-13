package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;

import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfigurationUpdateHandler;
import es.amplia.oda.event.api.ResponseDispatcherProxy;

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
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
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
        PowerMockito.whenNew(MqttClientFactoryProxy.class).withAnyArguments().thenReturn(mockedMqttClientFactory);
        PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
        PowerMockito.whenNew(DeviceInfoProviderProxy.class).withAnyArguments().thenReturn(mockedDeviceInfoProvider);
        PowerMockito.whenNew(ResponseDispatcherProxy.class).withAnyArguments().thenReturn(mockedResponseDispatcher);
        PowerMockito.whenNew(EventPublisherProxy.class).withAnyArguments().thenReturn(mockedEventPublisher);
        PowerMockito.whenNew(MqttDatastreamsOrchestrator.class).withAnyArguments().thenReturn(mockedOrchestrator);
        PowerMockito.whenNew(MqttDatastreamsConfigurationUpdateHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        when(mockedContext.registerService(eq(MqttDatastreamsService.class), any(), any())).thenReturn(mockedRegistration);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(MqttClientFactoryProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.JSON));
        PowerMockito.verifyNew(EventPublisherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(MqttDatastreamsOrchestrator.class).withArguments(eq(mockedMqttClientFactory),
                eq(mockedSerializer), eq(mockedEventPublisher), eq(mockedDeviceInfoProvider), eq(mockedResponseDispatcher), eq(mockedContext));
        PowerMockito.verifyNew(MqttDatastreamsConfigurationUpdateHandler.class).withArguments(eq(mockedOrchestrator));
        verify(mockedContext).registerService(eq(MqttDatastreamsService.class), any(), any());
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(MqttClientFactory.class), any());
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