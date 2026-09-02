package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.ruleengine.api.RuleEngineProxy;
import es.amplia.oda.statemanager.inmemory.configuration.StateManagerInMemoryConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private DatastreamsSettersFinderImpl mockedSettersFinder;
    @Mock
    private DatastreamsGettersFinderImpl mockedGettersFinder;
    @Mock
    private EventDispatcherProxy mockedEventDispatcherProxy;
    @Mock
    private InMemoryStateManager mockedStateManager;
    @Mock
    private ServiceRegistration<StateManager> mockedRegistration;
    @Mock
    private RuleEngineProxy mockedRuleEngine;
    @Mock
    private ConfigurableBundleImpl mockedBundle;
    @Mock
    private SchedulerImpl mockedScheduler;



    @Test
    public void testStart() throws Exception {
        List<List<?>> locatorArgs = new ArrayList<>();
        List<List<?>> gettersFinderArgs = new ArrayList<>();
        List<List<?>> settersFinderArgs = new ArrayList<>();
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> serializerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();

        try (MockedConstruction<ServiceLocatorOsgi> locatorCons = mockConstruction(ServiceLocatorOsgi.class,
                     (mock, mctx) -> locatorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamsGettersFinderImpl> gettersFinderCons = mockConstruction(DatastreamsGettersFinderImpl.class,
                     (mock, mctx) -> gettersFinderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamsSettersFinderImpl> settersFinderCons = mockConstruction(DatastreamsSettersFinderImpl.class,
                     (mock, mctx) -> settersFinderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<InMemoryStateManager> stateManagerCons = mockConstruction(InMemoryStateManager.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<RuleEngineProxy> ruleEngineCons = mockConstruction(RuleEngineProxy.class);
             MockedConstruction<EventDispatcherProxy> eventDispatcherCons = mockConstruction(EventDispatcherProxy.class);
             MockedConstruction<SerializerProxy> serializerCons = mockConstruction(SerializerProxy.class,
                     (mock, mctx) -> serializerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<StateManagerInMemoryConfigurationHandler> configHandlerCons = mockConstruction(StateManagerInMemoryConfigurationHandler.class,
                     (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ThreadPoolExecutor> executorCons = mockConstruction(ThreadPoolExecutor.class);
             MockedConstruction<SchedulerImpl> schedulerCons = mockConstruction(SchedulerImpl.class)) {

            testActivator.start(mockedContext);

            assertEquals(2, locatorCons.constructed().size());
            assertEquals(Arrays.asList(mockedContext, DatastreamsGetter.class), locatorArgs.get(0));
            assertEquals(Arrays.asList(mockedContext, DatastreamsSetter.class), locatorArgs.get(1));
            assertEquals(Collections.singletonList(locatorCons.constructed().get(0)), gettersFinderArgs.get(0));
            assertEquals(Collections.singletonList(locatorCons.constructed().get(1)), settersFinderArgs.get(0));

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(Arrays.asList(gettersFinderCons.constructed().get(0), settersFinderCons.constructed().get(0),
                    eventDispatcherCons.constructed().get(0), ruleEngineCons.constructed().get(0),
                    serializerCons.constructed().get(0), executorCons.constructed().get(0),
                    schedulerCons.constructed().get(0), mockedContext), stateManagerArgs.get(0));
            assertEquals(Arrays.asList(mockedContext, ContentType.JSON), serializerArgs.get(0));
            assertEquals(Collections.singletonList(stateManagerCons.constructed().get(0)), configHandlerArgs.get(0));
            assertEquals(Arrays.asList(mockedContext, configHandlerCons.constructed().get(0)), configurableBundleArgs.get(0));
            verify(mockedContext).registerService(eq(StateManager.class), eq(stateManagerCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "datastreamsGettersFinder", mockedGettersFinder);
        Whitebox.setInternalState(testActivator, "datastreamsSettersFinder", mockedSettersFinder);
        Whitebox.setInternalState(testActivator, "stateManagerRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "ruleEngine", mockedRuleEngine);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcherProxy);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedBundle);
        Whitebox.setInternalState(testActivator, "scheduler", mockedScheduler);
        Whitebox.setInternalState(testActivator, "inMemoryStateManager", mockedStateManager);

        testActivator.stop(mockedContext);

        verify(mockedBundle).close();
        verify(mockedEventDispatcherProxy).close();
        verify(mockedRegistration).unregister();
        verify(mockedGettersFinder).close();
        verify(mockedSettersFinder).close();
        verify(mockedRuleEngine).close();
        verify(mockedScheduler).close();
        verify(mockedStateManager).close();
    }
}
