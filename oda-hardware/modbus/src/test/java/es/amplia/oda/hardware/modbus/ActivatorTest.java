package es.amplia.oda.hardware.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.modbus.configuration.ModbusMasterConfigurationUpdateHandler;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;

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
    private ModbusMasterManager mockedModbusMasterManager;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;

    @Test
    public void testStart() throws Exception {
        List<List<?>> registrationManagerArgs = new ArrayList<>();
        List<List<?>> managerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();

        try (MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class,
                             (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ModbusMasterManager> managerCons = mockConstruction(ModbusMasterManager.class,
                     (mock, mctx) -> managerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ModbusMasterFactory> factoryCons = mockConstruction(ModbusMasterFactory.class);
             MockedConstruction<ModbusMasterConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(ModbusMasterConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, registrationManagerCons.constructed().size());
            assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
            assertEquals(ModbusMaster.class, registrationManagerArgs.get(0).get(1));
            assertEquals(1, managerCons.constructed().size());
            assertEquals(registrationManagerCons.constructed().get(0), managerArgs.get(0).get(0));
            assertEquals(1, factoryCons.constructed().size());
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(managerCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(factoryCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "modbusMasterManager", mockedModbusMasterManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);

        testActivator.stop(mockedContext);

        verify(mockedModbusMasterManager).close();
        verify(mockedConfigurableBundle).close();
    }
}
