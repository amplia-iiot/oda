package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.operation.synchronizeclock.configuration.SynchronizeConfigurationHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private StateManagerProxy mockedStateManager;
    @Mock
    private ServiceRegistration<OperationSynchronizeClock> mockedRegistration;
    @Mock
    private ConfigurableBundleImpl mockedBundle;

    @Test
    public void testStart() throws Exception {
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> synchronizeClockArgs = new ArrayList<>();
        List<List<?>> handlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationSynchronizeClockImpl> synchronizeClockCons =
                     mockConstruction(OperationSynchronizeClockImpl.class,
                             (mock, mctx) -> synchronizeClockArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SynchronizeConfigurationHandler> handlerCons =
                     mockConstruction(SynchronizeConfigurationHandler.class,
                             (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(mockedContext, stateManagerArgs.get(0).get(0));
            assertEquals(1, synchronizeClockCons.constructed().size());
            assertEquals(stateManagerCons.constructed().get(0), synchronizeClockArgs.get(0).get(0));
            assertEquals(1, handlerCons.constructed().size());
            assertEquals(synchronizeClockCons.constructed().get(0), handlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(OperationSynchronizeClock.class),
                    eq(synchronizeClockCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedBundle);
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "synchronizeClockRegistration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedBundle).close();
        verify(mockedRegistration).unregister();
        verify(mockedStateManager).close();
    }
}
