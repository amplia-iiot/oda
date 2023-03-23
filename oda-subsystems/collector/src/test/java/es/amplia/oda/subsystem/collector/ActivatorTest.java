package es.amplia.oda.subsystem.collector;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.subsystem.collector.configuration.CollectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.SchedulerImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ScheduledExecutorService;

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
    private StateManagerProxy mockedStateManager;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;
    @Mock
    private CollectorImpl mockedCollector;
    @Mock
    private SchedulerImpl mockedScheduler;
    @Mock
    private CollectorConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(StateManagerProxy.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcher);
        PowerMockito.whenNew(CollectorImpl.class).withAnyArguments().thenReturn(mockedCollector);
        PowerMockito.whenNew(SchedulerImpl.class).withAnyArguments().thenReturn(mockedScheduler);
        PowerMockito.whenNew(CollectorConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(StateManagerProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(EventDispatcherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(CollectorImpl.class).withArguments(eq(mockedStateManager), eq(mockedEventDispatcher));
        PowerMockito.verifyNew(SchedulerImpl.class).withArguments(any(ScheduledExecutorService.class));
        PowerMockito.verifyNew(CollectorConfigurationUpdateHandler.class).withArguments(eq(mockedCollector), eq(mockedScheduler));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcher);
        Whitebox.setInternalState(testActivator, "scheduler", mockedScheduler);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);

        testActivator.stop(mockedContext);

        verify(mockedConfigBundle).close();
        verify(mockedScheduler).close();
        verify(mockedStateManager).close();
        verify(mockedEventDispatcher).close();
    }
}