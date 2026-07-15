package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private StateManagerProxy mockedStateManager;
    @Mock
    private ServiceRegistration<OperationGetDeviceParameters> mockedRegistration;
    @Mock
    private DatastreamsGettersFinderImpl mockedDatastreamsGettersFinder;

    @Test
    public void testStart() throws Exception {
        List<List<?>> stateManagerArgs = new ArrayList<>();
        List<List<?>> getDeviceParametersArgs = new ArrayList<>();
        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class,
                     (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<DatastreamsGettersFinderImpl> gettersFinderCons =
                     mockConstruction(DatastreamsGettersFinderImpl.class);
             MockedConstruction<OperationGetDeviceParametersImpl> getDeviceParametersCons =
                     mockConstruction(OperationGetDeviceParametersImpl.class,
                             (mock, mctx) -> getDeviceParametersArgs.add(new ArrayList<>(mctx.arguments())))) {
            when(mockedContext.registerService(eq(OperationGetDeviceParameters.class), any(OperationGetDeviceParameters.class), any()))
                    .thenReturn(mockedRegistration);

            testActivator.start(mockedContext);

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(mockedContext, stateManagerArgs.get(0).get(0));
            assertEquals(1, getDeviceParametersCons.constructed().size());
            assertEquals(stateManagerCons.constructed().get(0), getDeviceParametersArgs.get(0).get(0));
            assertEquals(gettersFinderCons.constructed().get(0), getDeviceParametersArgs.get(0).get(1));
            verify(mockedContext).registerService(eq(OperationGetDeviceParameters.class),
                    eq(getDeviceParametersCons.constructed().get(0)), any());
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);
        Whitebox.setInternalState(testActivator, "registration", mockedRegistration);
        Whitebox.setInternalState(testActivator, "datastreamsGettersFinder", mockedDatastreamsGettersFinder);

        testActivator.stop(mockedContext);

        verify(mockedRegistration).unregister();
        verify(mockedStateManager).close();
        verify(mockedDatastreamsGettersFinder).close();
    }
}
