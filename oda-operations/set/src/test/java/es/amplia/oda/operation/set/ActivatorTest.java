package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

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
    private ServiceRegistration<OperationSetDeviceParameters> mockedRegistration;

    @Test
    public void testStart() throws Exception {
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> setDeviceParametersArgs = new ArrayList<>();
        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<OperationSetDeviceParametersImpl> setDeviceParametersCons =
                     mockConstruction(OperationSetDeviceParametersImpl.class,
                             (mock, mctx) -> setDeviceParametersArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(mockedContext, stateManagerArgs.get(0).get(0));
            assertEquals(1, setDeviceParametersCons.constructed().size());
            assertEquals(stateManagerCons.constructed().get(0), setDeviceParametersArgs.get(0).get(0));
            verify(mockedContext)
                    .registerService(eq(OperationSetDeviceParameters.class),
                            eq(setDeviceParametersCons.constructed().get(0)), any());
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
