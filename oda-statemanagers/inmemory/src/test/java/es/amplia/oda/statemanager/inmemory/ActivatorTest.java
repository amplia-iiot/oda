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
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ThreadPoolExecutor;

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
    private ServiceLocatorOsgi<DatastreamsSetter> mockedSetterLocator;
    @Mock
    private DatastreamsSettersFinderImpl mockedSettersFinder;
    @Mock
    private ServiceLocatorOsgi<DatastreamsGetter> mockedGetterLocator;
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
    private SerializerProxy mockedSerializer;
    @Mock
    private ConfigurableBundleImpl mockedBundle;
    @Mock
    private StateManagerInMemoryConfigurationHandler mockedConfigHandler;
    @Mock
    private ThreadPoolExecutor mockedExecutorService;
    @Mock
    private SchedulerImpl mockedScheduler;



    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ServiceLocatorOsgi.class).withArguments(any(BundleContext.class), eq(DatastreamsSetter.class)).thenReturn(mockedSetterLocator);
        PowerMockito.whenNew(DatastreamsSettersFinderImpl.class).withAnyArguments().thenReturn(mockedSettersFinder);
        PowerMockito.whenNew(ServiceLocatorOsgi.class).withArguments(any(BundleContext.class), eq(DatastreamsGetter.class)).thenReturn(mockedGetterLocator);
        PowerMockito.whenNew(DatastreamsGettersFinderImpl.class).withAnyArguments().thenReturn(mockedGettersFinder);
        PowerMockito.whenNew(InMemoryStateManager.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(RuleEngineProxy.class).withAnyArguments().thenReturn(mockedRuleEngine);
        PowerMockito.whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcherProxy);
        PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedBundle);
        PowerMockito.whenNew(StateManagerInMemoryConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ThreadPoolExecutor.class).withAnyArguments().thenReturn(mockedExecutorService);
        PowerMockito.whenNew(SchedulerImpl.class).withAnyArguments().thenReturn(mockedScheduler);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(DatastreamsGettersFinderImpl.class).withArguments(eq(mockedGetterLocator));
        PowerMockito.verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(DatastreamsSettersFinderImpl.class).withArguments(eq(mockedSetterLocator));

        PowerMockito.verifyNew(InMemoryStateManager.class).withArguments(eq(mockedGettersFinder), eq(mockedSettersFinder),
                eq(mockedEventDispatcherProxy), eq(mockedRuleEngine), eq(mockedSerializer),
                eq(mockedExecutorService), eq(mockedScheduler));
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.JSON));
        PowerMockito.verifyNew(StateManagerInMemoryConfigurationHandler.class).withArguments(eq(mockedStateManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(StateManager.class), eq(mockedStateManager), any());
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