package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;

import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfigurationUpdateHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
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
    private BundleContext mockedContext;
    @Mock
    private MqttClientFactoryProxy mockedMqttClientFactory;
    @Mock
    private SerializerProxy mockedSerializer;
    @Mock
    private EventPublisherProxy mockedEventPublisher;
    @Mock
    private ServiceRegistrationManagerWithKeyOsgi<String, DatastreamsGetter> mockedGetterRegistrationManager;
    @Mock
    private ServiceRegistrationManagerWithKeyOsgi<String, DatastreamsSetter> mockedSetterRegistrationManager;
    @Mock
    private MqttDatastreamsOrchestrator mockedOrchestrator;
    @Mock
    private MqttDatastreamsConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceListenerBundle<MqttClientFactory> mockedListener;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(MqttClientFactoryProxy.class).withAnyArguments().thenReturn(mockedMqttClientFactory);
        PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
        PowerMockito.whenNew(EventPublisherProxy.class).withAnyArguments().thenReturn(mockedEventPublisher);
        PowerMockito.whenNew(ServiceRegistrationManagerWithKeyOsgi.class)
                .withArguments(any(BundleContext.class), eq(DatastreamsGetter.class))
                .thenReturn(mockedGetterRegistrationManager);
        PowerMockito.whenNew(ServiceRegistrationManagerWithKeyOsgi.class)
                .withArguments(any(BundleContext.class), eq(DatastreamsSetter.class))
                .thenReturn(mockedSetterRegistrationManager);
        PowerMockito.whenNew(MqttDatastreamsOrchestrator.class).withAnyArguments().thenReturn(mockedOrchestrator);
        PowerMockito.whenNew(MqttDatastreamsConfigurationUpdateHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(MqttClientFactoryProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.CBOR));
        PowerMockito.verifyNew(EventPublisherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ServiceRegistrationManagerWithKeyOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(ServiceRegistrationManagerWithKeyOsgi.class)
                .withArguments(eq(mockedContext),eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(MqttDatastreamsOrchestrator.class).withArguments(eq(mockedMqttClientFactory),
                eq(mockedSerializer), eq(mockedEventPublisher), eq(mockedGetterRegistrationManager),
                eq(mockedSetterRegistrationManager));
        PowerMockito.verifyNew(MqttDatastreamsConfigurationUpdateHandler.class).withArguments(eq(mockedOrchestrator));
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
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "mqttClientFactoryListener", mockedListener);

        testActivator.stop(mockedContext);

        verify(mockedListener).close();
        verify(mockedConfigBundle).close();
        verify(mockedOrchestrator).close();
        verify(mockedMqttClientFactory).close();
        verify(mockedSerializer).close();
        verify(mockedEventPublisher).close();
    }
}