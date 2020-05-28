package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.ruleengine.api.RuleEngineProxy;
import es.amplia.oda.statemanager.api.OsgiEventHandler;
import es.amplia.oda.statemanager.api.StateManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
    private OsgiEventHandler mockedEventHandler;
    @Mock
    private EventDispatcherProxy mockedEventDispatcherProxy;
    @Mock
    private InMemoryStateManager mockedStateManager;
    @Mock
    private ServiceRegistration<StateManager> mockedRegistration;
    @Mock
    private RuleEngineProxy mockedRuleEngine;


    /*@Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ServiceLocatorOsgi.class).withAnyArguments().thenReturn(mockedSetterLocator);
        PowerMockito.whenNew(DatastreamsSettersFinderImpl.class).withAnyArguments().thenReturn(mockedSettersFinder);
        PowerMockito.whenNew(OsgiEventHandler.class).withAnyArguments().thenReturn(mockedEventHandler);
        PowerMockito.whenNew(InMemoryStateManager.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(RuleEngineProxy.class).withAnyArguments().thenReturn(mockedRuleEngine);
        PowerMockito.whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcherProxy);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(DatastreamsSettersFinderImpl.class).withArguments(eq(mockedSetterLocator));
        PowerMockito.verifyNew(OsgiEventHandler.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(InMemoryStateManager.class).withArguments(eq(mockedSettersFinder), eq(mockedEventDispatcherProxy), eq(mockedEventHandler), eq(mockedRuleEngine));
        verify(mockedContext).registerService(eq(StateManager.class), eq(mockedStateManager), any());
    }*/

    @Test
    public void testStop() {
        /*Whitebox.setInternalState(testActivator, "datastreamsSettersFinder", mockedSettersFinder);
        Whitebox.setInternalState(testActivator, "eventHandler", mockedEventHandler);
        Whitebox.setInternalState(testActivator, "stateManagerRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "ruleEngine", mockedRuleEngine);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcherProxy);

        testActivator.stop(mockedContext);

        verify(mockedEventDispatcherProxy).close();
        verify(mockedRegistration).unregister();
        verify(mockedSettersFinder).close();
        verify(mockedRuleEngine).close();
        verify(mockedEventHandler).close();*/
    }
}