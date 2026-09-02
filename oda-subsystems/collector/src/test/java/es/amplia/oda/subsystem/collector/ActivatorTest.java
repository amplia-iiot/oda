package es.amplia.oda.subsystem.collector;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.subsystem.collector.configuration.CollectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.SchedulerImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private StateManagerProxy mockedStateManager;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;
    @Mock
    private SchedulerImpl mockedScheduler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;

    @Test
    public void testStart() throws Exception {
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> eventDispatcherArgs = new ArrayList<>();
        List<List<?>> collectorArgs = new ArrayList<>();
        List<List<?>> schedulerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();

        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventDispatcherProxy> eventDispatcherCons = mockConstruction(EventDispatcherProxy.class,
                     (mock, mctx) -> eventDispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CollectorImpl> collectorCons = mockConstruction(CollectorImpl.class,
                     (mock, mctx) -> collectorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SchedulerImpl> schedulerCons = mockConstruction(SchedulerImpl.class,
                     (mock, mctx) -> schedulerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CollectorConfigurationUpdateHandler> configHandlerCons = mockConstruction(CollectorConfigurationUpdateHandler.class,
                     (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(Collections.singletonList(mockedContext), stateManagerArgs.get(0));
            assertEquals(Collections.singletonList(mockedContext), eventDispatcherArgs.get(0));
            assertEquals(Arrays.asList(stateManagerCons.constructed().get(0),
                    eventDispatcherCons.constructed().get(0)), collectorArgs.get(0));
            assertEquals(1, schedulerCons.constructed().size());
            assertTrue(schedulerArgs.get(0).get(0) instanceof ScheduledExecutorService);
            assertEquals(Arrays.asList(collectorCons.constructed().get(0),
                    schedulerCons.constructed().get(0)), configHandlerArgs.get(0));
            assertEquals(Arrays.asList(mockedContext, configHandlerCons.constructed().get(0)), configBundleArgs.get(0));
        }
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
