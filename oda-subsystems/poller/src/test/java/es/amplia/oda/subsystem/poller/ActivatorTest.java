package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.utils.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private final Activator activator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private ServiceLocatorOsgi<DatastreamsGetter> mockedGettersLocator;
	@Mock
	private DatastreamsGettersFinderImpl mockedGettersFinder;
	@Mock
	private EventPublisherProxy mockedEventPublisher;
	@Mock
	private PollerDatastreamsEvent mockedDatastreamsEvent;
	@Mock
	private PollerImpl mockedPoller;
	@Mock
	private SchedulerImpl mockedScheduler;
	@Mock
	private PollerConfigurationUpdateHandler mockedHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;

	@Test
	public void testStart() throws Exception {
		whenNew(ServiceLocatorOsgi.class).withAnyArguments().thenReturn(mockedGettersLocator);
		whenNew(DatastreamsGettersFinderImpl.class).withAnyArguments().thenReturn(mockedGettersFinder);
		whenNew(EventPublisherProxy.class).withAnyArguments().thenReturn(mockedEventPublisher);
		whenNew(PollerDatastreamsEvent.class).withAnyArguments().thenReturn(mockedDatastreamsEvent);
		whenNew(PollerImpl.class).withAnyArguments().thenReturn(mockedPoller);
		whenNew(SchedulerImpl.class).withAnyArguments().thenReturn(mockedScheduler);
		whenNew(PollerConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);

		activator.start(mockedContext);

		verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext),eq(DatastreamsGetter.class));
		verifyNew(DatastreamsGettersFinderImpl.class).withArguments(eq(mockedGettersLocator));
		verifyNew(EventPublisherProxy.class).withArguments(eq(mockedContext));
		verifyNew(PollerImpl.class).withArguments(any(), any());
		verifyNew(PollerConfigurationUpdateHandler.class).withArguments(any(), any());
		verifyNew(ConfigurableBundleImpl.class).withArguments(any(), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "datastreamsGettersFinder", mockedGettersFinder);
		Whitebox.setInternalState(activator, "eventPublisher", mockedEventPublisher);
		Whitebox.setInternalState(activator, "scheduler", mockedScheduler);
		Whitebox.setInternalState(activator, "configurableBundle", mockedConfigurableBundle);

		activator.stop(mockedContext);

		verify(mockedConfigurableBundle).close();
		verify(mockedScheduler).close();
		verify(mockedGettersFinder).close();
		verify(mockedEventPublisher).close();
	}
}
