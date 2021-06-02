package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.MqttDatastreamsServiceProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.operation.api.OperationDiscover;
import es.amplia.oda.operation.localprotocoldiscovery.configuration.LocalProtocolDiscoveryConfigurationUpdateHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private MqttClientFactoryProxy mockedFactory;
	@Mock
	private MqttDatastreamsServiceProxy mockedMqttDatastreamsService;
	@Mock
	private SerializerProxy mockedSerializer;
	@Mock
	private OperationLocalProtocolDiscoveryImpl mockedOperation;
	@Mock
	private LocalProtocolDiscoveryConfigurationUpdateHandler mockedConfigHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigBundle;
	@Mock
	private ServiceListenerBundle<MqttClientFactory> mockedListener;
	@Mock
	private ServiceListenerBundle<MqttDatastreamsService> mockedDatastreamsListener;
	@Mock
	private ServiceListenerBundle<Serializer> mockedSerializerListener;
	@Mock
	private ServiceRegistration<OperationDiscover> mockedRegistration;

	@Test
	public void testStart() throws Exception {
		PowerMockito.whenNew(MqttClientFactoryProxy.class).withAnyArguments().thenReturn(mockedFactory);
		PowerMockito.whenNew(MqttDatastreamsServiceProxy.class).withAnyArguments()
				.thenReturn(mockedMqttDatastreamsService);
		PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
		PowerMockito.whenNew(OperationLocalProtocolDiscoveryImpl.class).withAnyArguments().thenReturn(mockedOperation);
		PowerMockito.whenNew(LocalProtocolDiscoveryConfigurationUpdateHandler.class).withAnyArguments()
				.thenReturn(mockedConfigHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);
		PowerMockito.whenNew(ServiceListenerBundle.class)
				.withArguments(eq(mockedContext), eq(MqttClientFactory.class), any()).thenReturn(mockedListener);
		PowerMockito.whenNew(ServiceListenerBundle.class)
				.withArguments(eq(mockedContext), eq(MqttDatastreamsService.class), any()).thenReturn(mockedDatastreamsListener);
		PowerMockito.whenNew(ServiceListenerBundle.class)
				.withArguments(eq(mockedContext), eq(Serializer.class), any()).thenReturn(mockedSerializerListener);

		testActivator.start(mockedContext);

		PowerMockito.verifyNew(MqttClientFactoryProxy.class).withArguments(eq(mockedContext));
		PowerMockito.verifyNew(MqttDatastreamsServiceProxy.class).withArguments(eq(mockedContext));
		PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.CBOR));
		PowerMockito.verifyNew(OperationLocalProtocolDiscoveryImpl.class)
				.withArguments(eq(mockedFactory), eq(mockedMqttDatastreamsService), eq(mockedSerializer));
		PowerMockito.verifyNew(LocalProtocolDiscoveryConfigurationUpdateHandler.class)
				.withArguments(eq(mockedOperation));
		PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
		verify(mockedContext).registerService(eq(OperationDiscover.class), eq(mockedOperation), any());
		PowerMockito.verifyNew(ServiceListenerBundle.class)
				.withArguments(eq(mockedContext), eq(MqttClientFactory.class), any(Runnable.class));
		PowerMockito.verifyNew(ServiceListenerBundle.class)
				.withArguments(eq(mockedContext), eq(MqttDatastreamsService.class), any(Runnable.class));
		PowerMockito.verifyNew(ServiceListenerBundle.class)
				.withArguments(eq(mockedContext), eq(Serializer.class), any(Runnable.class));
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

		testActivator.onServiceChanged();

		verify(mockedConfigHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithExceptionIsCaught() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);
		Mockito.doThrow(Exception.class).when(mockedConfigHandler).applyConfiguration();

		testActivator.onServiceChanged();

		assertTrue("Exception should be caught", true);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "serializerServiceListener", mockedSerializerListener);
		Whitebox.setInternalState(testActivator, "mqttDatastreamsServiceListener", mockedDatastreamsListener);
		Whitebox.setInternalState(testActivator, "mqttClientFactoryListener", mockedListener);
		Whitebox.setInternalState(testActivator, "registration", mockedRegistration);
		Whitebox.setInternalState(testActivator, "mqttClientFactory", mockedFactory);
		Whitebox.setInternalState(testActivator, "mqttDatastreamsService", mockedMqttDatastreamsService);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
		Whitebox.setInternalState(testActivator, "serializer", mockedSerializer);

		testActivator.stop(mockedContext);

		verify(mockedSerializerListener).close();
		verify(mockedDatastreamsListener).close();
		verify(mockedListener).close();
		verify(mockedRegistration).unregister();
		verify(mockedFactory).close();
		verify(mockedMqttDatastreamsService).close();
		verify(mockedConfigBundle).close();
		verify(mockedSerializer).close();
	}
}
