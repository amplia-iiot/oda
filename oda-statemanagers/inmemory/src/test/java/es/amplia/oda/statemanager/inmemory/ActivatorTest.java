package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
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


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ServiceLocatorOsgi.class).withAnyArguments().thenReturn(mockedSetterLocator);
        PowerMockito.whenNew(DatastreamsSettersFinderImpl.class).withAnyArguments().thenReturn(mockedSettersFinder);
        PowerMockito.whenNew(InMemoryStateManager.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(RuleEngineProxy.class).withAnyArguments().thenReturn(mockedRuleEngine);
        PowerMockito.whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcherProxy);
        PowerMockito.whenNew(SerializerProxy.class).withAnyArguments().thenReturn(mockedSerializer);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedBundle);
        PowerMockito.whenNew(StateManagerInMemoryConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ThreadPoolExecutor.class).withAnyArguments().thenReturn(mockedExecutorService);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(DatastreamsSettersFinderImpl.class).withArguments(eq(mockedSetterLocator));
        PowerMockito.verifyNew(InMemoryStateManager.class).withArguments(eq(mockedSettersFinder),
                eq(mockedEventDispatcherProxy), eq(mockedRuleEngine), eq(mockedSerializer), eq(mockedExecutorService));
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.JSON));
        PowerMockito.verifyNew(StateManagerInMemoryConfigurationHandler.class).withArguments(eq(mockedStateManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(StateManager.class), eq(mockedStateManager), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "datastreamsSettersFinder", mockedSettersFinder);
        Whitebox.setInternalState(testActivator, "stateManagerRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "ruleEngine", mockedRuleEngine);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcherProxy);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedBundle);

        testActivator.stop(mockedContext);

        verify(mockedBundle).close();
        verify(mockedEventDispatcherProxy).close();
        verify(mockedRegistration).unregister();
        verify(mockedSettersFinder).close();
        verify(mockedRuleEngine).close();
    }
}