package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.simulator.configuration.SimulatedDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsSetterFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private SimulatedDatastreamsManager mockedDatastreamsManager;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;


    @Test
    public void testStart() throws Exception {
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> datastreamsManagerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();
        try (MockedConstruction<SimulatedDatastreamsGetterFactory> getterFactoryCons =
                     mockConstruction(SimulatedDatastreamsGetterFactory.class);
             MockedConstruction<SimulatedDatastreamsSetterFactory> setterFactoryCons =
                     mockConstruction(SimulatedDatastreamsSetterFactory.class);
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SimulatedDatastreamsManager> datastreamsManagerCons =
                     mockConstruction(SimulatedDatastreamsManager.class,
                             (mock, mctx) -> datastreamsManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SimulatedDatastreamsConfigurationHandler> configHandlerCons =
                     mockConstruction(SimulatedDatastreamsConfigurationHandler.class);
             MockedConstruction<ConfigurableBundleImpl> configBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, getterFactoryCons.constructed().size());
            assertEquals(1, setterFactoryCons.constructed().size());
            assertEquals(2, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(DatastreamsGetter.class, registrationManagerArgs.get(0).get(1));
            assertEquals(mockedContext, registrationManagerArgs.get(1).get(0));
            assertEquals(DatastreamsSetter.class, registrationManagerArgs.get(1).get(1));
            assertEquals(1, datastreamsManagerCons.constructed().size());
            assertEquals(getterFactoryCons.constructed().get(0), datastreamsManagerArgs.get(0).get(0));
            assertEquals(setterFactoryCons.constructed().get(0), datastreamsManagerArgs.get(0).get(1));
            assertEquals(registrationManagerCons.constructed().get(0), datastreamsManagerArgs.get(0).get(2));
            assertEquals(registrationManagerCons.constructed().get(1), datastreamsManagerArgs.get(0).get(3));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "datastreamsManager", mockedDatastreamsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);

        testActivator.stop(mockedContext);

        verify(mockedConfigBundle).close();
        verify(mockedDatastreamsManager).close();
    }
}
