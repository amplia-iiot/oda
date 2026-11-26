package es.amplia.oda.datastreams.modbusslave;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.internal.ModbusSlaveManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ModbusSlaveManager mockedModbusSlaveManager;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private StateManagerProxy mockedStateManager;

    @Test
    public void testStart() {
        List<List<?>> managerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();

        try (MockedConstruction<StateManagerProxy> stateManagerCons = mockConstruction(StateManagerProxy.class);
             MockedConstruction<ModbusSlaveManager> managerCons = mockConstruction(ModbusSlaveManager.class,
                     (mock, mctx) -> managerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ModbusSlaveConfigurationUpdateHandler> configHandlerCons =
                     mockConstruction(ModbusSlaveConfigurationUpdateHandler.class,
                             (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, stateManagerCons.constructed().size());
            assertEquals(1, managerCons.constructed().size());
            assertEquals(stateManagerCons.constructed().get(0), managerArgs.get(0).get(0));
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(managerCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "modbusSlaveManager", mockedModbusSlaveManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "stateManager", mockedStateManager);

        testActivator.stop(mockedContext);

        verify(mockedStateManager).close();
        verify(mockedModbusSlaveManager).close();
        verify(mockedConfigurableBundle).close();
    }
}
