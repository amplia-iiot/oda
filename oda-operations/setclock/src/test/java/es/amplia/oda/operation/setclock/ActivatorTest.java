package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.OperationSetClock;
import es.amplia.oda.operation.setclock.configuration.SetClockConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
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
    private StateManagerProxy mockedStateManager;
    @Mock
    private ServiceRegistration<OperationSetClock> mockedRegistration;
    @Mock
    private ConfigurableBundleImpl mockedBundle;

    @Test
    public void testStart() throws Exception {
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> setClockArgs = new ArrayList<>();
        List<List<?>> handlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationSetClockImpl> setClockCons = mockConstruction(OperationSetClockImpl.class,
                     (mock, mctx) -> setClockArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SetClockConfigurationHandler> handlerCons =
                     mockConstruction(SetClockConfigurationHandler.class,
                             (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(mockedContext, stateManagerArgs.get(0).get(0));
            assertEquals(1, setClockCons.constructed().size());
            assertEquals(stateManagerCons.constructed().get(0), setClockArgs.get(0).get(0));
            assertEquals(1, handlerCons.constructed().size());
            assertEquals(setClockCons.constructed().get(0), handlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(OperationSetClock.class),
                    eq(setClockCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "setClockRegistration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedBundle);

        testActivator.stop(mockedContext);

        verify(mockedBundle).close();
        verify(mockedRegistration).unregister();
        verify(mockedStateManager).close();
    }
}
