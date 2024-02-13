package es.amplia.oda.datastreams.lora;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.osgi.proxies.UdpServiceProxy;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.lora.configuration.LoraDatastreamsConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

	@Mock
	UdpServiceProxy mockedService;
	@Mock
	SerializerProxy mockedSerializer;
	@Mock
	EventPublisherProxy mockedPublisher;
	@Mock
	LoraDatastreamsOrchestrator mockedOrchestrator;
	@Mock
	LoraDatastreamsConfigurationHandler mockedHandler;
	@Mock
	ServiceListenerBundle<UdpService> mockedServiceListener;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;

	private final Activator testActivator = new Activator();

	@Test
	public void testStart() throws Exception {
		whenNew(UdpServiceProxy.class).withAnyArguments().thenReturn(mockedService);
		whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
		whenNew(EventPublisherProxy.class).withAnyArguments().thenReturn(mockedPublisher);
		whenNew(LoraDatastreamsOrchestrator.class).withAnyArguments().thenReturn(mockedOrchestrator);
		whenNew(LoraDatastreamsConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedServiceListener);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);

		testActivator.start(mockedContext);

		verifyNew(UdpServiceProxy.class).withArguments(eq(mockedContext));
		verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.JSON));
		verifyNew(EventPublisherProxy.class).withArguments(eq(mockedContext));
		verifyNew(LoraDatastreamsOrchestrator.class)
				.withArguments(eq(mockedService), eq(mockedPublisher), eq(mockedSerializer));
		verifyNew(LoraDatastreamsConfigurationHandler.class).withArguments(eq(mockedOrchestrator));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler));
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(UdpService.class), any());
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(testActivator, "configurationHandler", mockedHandler);

		testActivator.onServiceChanged();

		verify(mockedHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithException() {
		Whitebox.setInternalState(testActivator, "configurationHandler", mockedHandler);
		doThrow(Exception.class).when(mockedHandler).applyConfiguration();

		testActivator.onServiceChanged();

		verify(mockedHandler).applyConfiguration();
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "udpServiceServiceListener", mockedServiceListener);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "loraDatastreamsOrchestrator", mockedOrchestrator);
		Whitebox.setInternalState(testActivator, "eventPublisher", mockedPublisher);
		Whitebox.setInternalState(testActivator, "serializer", mockedSerializer);
		Whitebox.setInternalState(testActivator, "udpService", mockedService);

		testActivator.stop(mockedContext);

		verify(mockedServiceListener).close();
		verify(mockedConfigurableBundle).close();
		verify(mockedOrchestrator).close();
		verify(mockedPublisher).close();
		verify(mockedSerializer).close();
		verify(mockedService).close();
	}
}
