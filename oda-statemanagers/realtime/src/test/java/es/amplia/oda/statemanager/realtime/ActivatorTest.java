package es.amplia.oda.statemanager.realtime;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceLocatorOsgi<DatastreamsGetter> mockedGettersLocator;
    @Mock
    private DatastreamsGettersFinderImpl mockedGettersFinder;
    @Mock
    private ServiceLocatorOsgi<DatastreamsSetter> mockedSettersLocator;
    @Mock
    private DatastreamsSettersFinderImpl mockedSettersFinder;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;
    @Mock
    private RealTimeStateManager mockedStateManager;
    @Mock
    ServiceRegistration<StateManager> mockedRegistration;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ServiceLocatorOsgi.class)
                .withArguments(any(BundleContext.class), eq(DatastreamsGetter.class)).thenReturn(mockedGettersLocator);
        PowerMockito.whenNew(DatastreamsGettersFinderImpl.class).withAnyArguments().thenReturn(mockedGettersFinder);
        PowerMockito.whenNew(ServiceLocatorOsgi.class)
                .withArguments(any(BundleContext.class), eq(DatastreamsSetter.class)).thenReturn(mockedSettersLocator);
        PowerMockito.whenNew(DatastreamsSettersFinderImpl.class).withAnyArguments().thenReturn(mockedSettersFinder);
        PowerMockito.whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcher);
        PowerMockito.whenNew(RealTimeStateManager.class).withAnyArguments().thenReturn(mockedStateManager);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(DatastreamsGettersFinderImpl.class).withArguments(eq(mockedGettersLocator));
        PowerMockito.verifyNew(ServiceLocatorOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(DatastreamsSettersFinderImpl.class).withArguments(eq(mockedSettersLocator));
        PowerMockito.verifyNew(EventDispatcherProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(RealTimeStateManager.class).withArguments(eq(mockedGettersFinder),
                eq(mockedSettersFinder), eq(mockedEventDispatcher));
        verify(mockedContext).registerService(eq(StateManager.class), eq(mockedStateManager), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "datastreamsGettersFinder", mockedGettersFinder);
        Whitebox.setInternalState(testActivator, "datastreamsSettersFinder", mockedSettersFinder);
        Whitebox.setInternalState(testActivator, "eventDispatcher", mockedEventDispatcher);
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedGettersFinder).close();
        verify(mockedSettersFinder).close();
        verify(mockedEventDispatcher).close();
    }
}