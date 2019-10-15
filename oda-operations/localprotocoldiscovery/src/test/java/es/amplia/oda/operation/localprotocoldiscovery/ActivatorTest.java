package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedBundleContext;
	@Mock
	private MqttClientFactoryProxy mockedMqttClientFactoryProxy;
	@Mock
	private SerializerProxy mockedSerializerProxy;
	@Mock
	private OperationLocalProtocolDiscoveryImpl mockedOperationLocalProtocolDiscoveryImpl;
	@Mock
	private LocalProtocolDiscoveryConfigurationUpdateHandler mockedLocalProtocolDiscoveryConfigurationUpdateHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundleImpl;
	@Mock
	private ServiceListenerBundle mockedServiceListenerBundle;
	@Mock
	private ServiceRegistration mockedServiceRegistration;

	@Test
	public void testStart() throws Exception {
		PowerMockito.whenNew(MqttClientFactoryProxy.class).withAnyArguments().thenReturn(mockedMqttClientFactoryProxy);
		PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializerProxy);
		PowerMockito.whenNew(OperationLocalProtocolDiscoveryImpl.class).withAnyArguments().thenReturn(mockedOperationLocalProtocolDiscoveryImpl);
		PowerMockito.whenNew(LocalProtocolDiscoveryConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedLocalProtocolDiscoveryConfigurationUpdateHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundleImpl);
		PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedServiceListenerBundle);

		testActivator.start(mockedBundleContext);

		PowerMockito.verifyNew(MqttClientFactoryProxy.class).withArguments(eq(mockedBundleContext));
		PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedBundleContext), any());
		PowerMockito.verifyNew(OperationLocalProtocolDiscoveryImpl.class).withArguments(eq(mockedMqttClientFactoryProxy), eq(mockedSerializerProxy));
		PowerMockito.verifyNew(LocalProtocolDiscoveryConfigurationUpdateHandler.class).withArguments(eq(mockedOperationLocalProtocolDiscoveryImpl));
		PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedBundleContext), eq(mockedLocalProtocolDiscoveryConfigurationUpdateHandler));
		PowerMockito.verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedBundleContext), any(), any());
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedLocalProtocolDiscoveryConfigurationUpdateHandler);

		testActivator.onServiceChanged();

		Mockito.verify(mockedLocalProtocolDiscoveryConfigurationUpdateHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithExceptionIsCaught() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedLocalProtocolDiscoveryConfigurationUpdateHandler);
		Mockito.doThrow(Exception.class).when(mockedLocalProtocolDiscoveryConfigurationUpdateHandler).applyConfiguration();

		testActivator.onServiceChanged();

		assertTrue("Exception should be caught", true);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "mqttClientFactoryListener", mockedServiceListenerBundle);
		Whitebox.setInternalState(testActivator, "registration", mockedServiceRegistration);
		Whitebox.setInternalState(testActivator, "mqttClientFactory", mockedMqttClientFactoryProxy);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundleImpl);
		Whitebox.setInternalState(testActivator, "serializer", mockedSerializerProxy);

		testActivator.stop(mockedBundleContext);

		Mockito.verify(mockedServiceListenerBundle).close();
		Mockito.verify(mockedServiceRegistration).unregister();
		Mockito.verify(mockedMqttClientFactoryProxy).close();
		Mockito.verify(mockedConfigurableBundleImpl).close();
		Mockito.verify(mockedSerializerProxy).close();
	}
}
