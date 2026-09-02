package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

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
    private ServiceRegistration<OperationRefreshInfo> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> refreshInfoArgs = new ArrayList<>();
        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationRefreshInfoImpl> refreshInfoCons =
                     mockConstruction(OperationRefreshInfoImpl.class,
                             (mock, mctx) -> refreshInfoArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(mockedContext, stateManagerArgs.get(0).get(0));
            assertEquals(1, refreshInfoCons.constructed().size());
            assertEquals(stateManagerCons.constructed().get(0), refreshInfoArgs.get(0).get(0));
            verify(mockedContext).registerService(eq(OperationRefreshInfo.class),
                    eq(refreshInfoCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedStateManager).close();
    }
}
