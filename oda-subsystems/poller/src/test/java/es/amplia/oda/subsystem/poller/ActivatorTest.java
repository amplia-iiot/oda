package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.utils.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

	private final Activator activator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private DatastreamsGettersFinderImpl mockedGettersFinder;
	@Mock
	private EventPublisherProxy mockedEventPublisher;
	@Mock
	private SchedulerImpl mockedScheduler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;

	@Test
	public void testStart() throws Exception {
		List<List<?>> locatorArgs = new ArrayList<>();
		List<List<?>> gettersFinderArgs = new ArrayList<>();
		List<List<?>> eventPublisherArgs = new ArrayList<>();

		try (MockedConstruction<ServiceLocatorOsgi> locatorCons = mockConstruction(ServiceLocatorOsgi.class,
					 (mock, mctx) -> locatorArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DatastreamsGettersFinderImpl> gettersFinderCons = mockConstruction(DatastreamsGettersFinderImpl.class,
					 (mock, mctx) -> gettersFinderArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<EventPublisherProxy> eventPublisherCons = mockConstruction(EventPublisherProxy.class,
					 (mock, mctx) -> eventPublisherArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<PollerDatastreamsEvent> datastreamsEventCons = mockConstruction(PollerDatastreamsEvent.class);
			 MockedConstruction<PollerImpl> pollerCons = mockConstruction(PollerImpl.class);
			 MockedConstruction<SchedulerImpl> schedulerCons = mockConstruction(SchedulerImpl.class);
			 MockedConstruction<PollerConfigurationUpdateHandler> configHandlerCons = mockConstruction(PollerConfigurationUpdateHandler.class);
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons = mockConstruction(ConfigurableBundleImpl.class)) {

			activator.start(mockedContext);

			assertEquals(Arrays.asList(mockedContext, DatastreamsGetter.class), locatorArgs.get(0));
			assertEquals(Collections.singletonList(locatorCons.constructed().get(0)), gettersFinderArgs.get(0));
			assertEquals(Collections.singletonList(mockedContext), eventPublisherArgs.get(0));
			assertEquals(1, pollerCons.constructed().size());
			assertEquals(1, configHandlerCons.constructed().size());
			assertEquals(1, configurableBundleCons.constructed().size());
		}
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
