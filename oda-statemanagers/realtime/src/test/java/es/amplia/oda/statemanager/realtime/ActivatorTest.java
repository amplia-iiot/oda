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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private DatastreamsGettersFinderImpl mockedGettersFinder;
    @Mock
    private DatastreamsSettersFinderImpl mockedSettersFinder;
    @Mock
    private EventDispatcherProxy mockedEventDispatcher;
    @Mock
    ServiceRegistration<StateManager> mockedRegistration;


    @Test
    public void testStart() throws Exception {
        List<List<?>> locatorArgs = new ArrayList<>();
        List<List<?>> gettersFinderArgs = new ArrayList<>();
        List<List<?>> settersFinderArgs = new ArrayList<>();
        List<List<?>> eventDispatcherArgs = new ArrayList<>();
        List<List<?>> stateManagerArgs = new ArrayList<>();

        try (MockedConstruction<ServiceLocatorOsgi> locatorCons = mockConstruction(ServiceLocatorOsgi.class,
                     (mock, mctx) -> locatorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamsGettersFinderImpl> gettersFinderCons = mockConstruction(DatastreamsGettersFinderImpl.class,
                     (mock, mctx) -> gettersFinderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamsSettersFinderImpl> settersFinderCons = mockConstruction(DatastreamsSettersFinderImpl.class,
                     (mock, mctx) -> settersFinderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<EventDispatcherProxy> eventDispatcherCons = mockConstruction(EventDispatcherProxy.class,
                     (mock, mctx) -> eventDispatcherArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<RealTimeStateManager> stateManagerCons = mockConstruction(RealTimeStateManager.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(2, locatorCons.constructed().size());
            assertEquals(Arrays.asList(mockedContext, DatastreamsGetter.class), locatorArgs.get(0));
            assertEquals(Arrays.asList(mockedContext, DatastreamsSetter.class), locatorArgs.get(1));
            assertEquals(Collections.singletonList(locatorCons.constructed().get(0)), gettersFinderArgs.get(0));
            assertEquals(Collections.singletonList(locatorCons.constructed().get(1)), settersFinderArgs.get(0));
            assertEquals(Collections.singletonList(mockedContext), eventDispatcherArgs.get(0));
            assertEquals(Arrays.asList(gettersFinderCons.constructed().get(0),
                    settersFinderCons.constructed().get(0), eventDispatcherCons.constructed().get(0)),
                    stateManagerArgs.get(0));
            verify(mockedContext).registerService(eq(StateManager.class), eq(stateManagerCons.constructed().get(0)), any());
        }
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
