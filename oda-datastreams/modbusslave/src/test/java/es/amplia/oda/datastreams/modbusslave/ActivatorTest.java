package es.amplia.oda.datastreams.modbusslave;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.internal.ModbusSlaveManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ModbusSlaveManager mockedModbusSlaveManager;
    @Mock
    private ModbusSlaveConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private StateManagerProxy mockedStateManager;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ModbusSlaveManager.class).withAnyArguments().thenReturn(mockedModbusSlaveManager);
        PowerMockito.whenNew(StateManagerProxy.class).withAnyArguments().thenReturn(mockedStateManager);
        PowerMockito.whenNew(ModbusSlaveConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ModbusSlaveManager.class).withArguments(eq(mockedStateManager));
        PowerMockito.verifyNew(ModbusSlaveConfigurationUpdateHandler.class).withArguments(eq(mockedModbusSlaveManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
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