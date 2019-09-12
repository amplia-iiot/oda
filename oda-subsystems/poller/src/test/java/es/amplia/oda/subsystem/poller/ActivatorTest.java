package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocatorOsgi;
import es.amplia.oda.event.api.EventDispatcherProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private Activator activator = new Activator();

	@Mock
	BundleContext mockedContext;
	@Mock
	DatastreamsGettersLocatorOsgi mockedLocator;
	@Mock
	DatastreamsGetterFinderImpl mockedFinder;
	@Mock
	EventDispatcherProxy mockedDispatcher;
	@Mock
	PollerImpl mockedPoller;
	@Mock
	PollerConfigurationUpdateHandler mockedHandler;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	Bundle mockedBundle;
	@Mock
	ScheduledExecutorService mockedExecutor;
	@Mock
	Logger mockedLogger;

	@Test
	public void testStart() throws Exception {
		whenNew(DatastreamsGettersLocatorOsgi.class).withAnyArguments().thenReturn(mockedLocator);
		whenNew(DatastreamsGetterFinderImpl.class).withAnyArguments().thenReturn(mockedFinder);
		whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedDispatcher);
		whenNew(PollerImpl.class).withAnyArguments().thenReturn(mockedPoller);
		whenNew(PollerConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		when(mockedContext.getBundle()).thenReturn(mockedBundle);
		when(mockedBundle.getSymbolicName()).thenReturn("name");

		activator.start(mockedContext);

		verifyNew(DatastreamsGettersLocatorOsgi.class).withArguments(any());
		verifyNew(DatastreamsGetterFinderImpl.class).withArguments(any());
		verifyNew(EventDispatcherProxy.class).withArguments(any());
		verifyNew(PollerImpl.class).withArguments(any(), any());
		verifyNew(PollerConfigurationUpdateHandler.class).withArguments(any(), any());
		verifyNew(ConfigurableBundleImpl.class).withArguments(any(), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(activator, "eventDispatcher", mockedDispatcher);

		activator.stop(mockedContext);

		verify(mockedConfigurableBundle).close();
		verify(mockedDispatcher).close();
	}

	@Test
	public void testStopPendingOperations() {
		Whitebox.setInternalState(activator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(activator, "eventDispatcher", mockedDispatcher);
		Whitebox.setInternalState(activator, "executor", mockedExecutor);

		activator.stop(mockedContext);

		verify(mockedExecutor).shutdown();
	}
}
