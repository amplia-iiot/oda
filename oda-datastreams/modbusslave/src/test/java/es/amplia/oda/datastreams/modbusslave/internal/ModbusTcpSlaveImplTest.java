package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import es.amplia.oda.core.commons.interfaces.StateManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusTCPSlaveImpl.class)
public class ModbusTcpSlaveImplTest {

    private static final String TEST_ADDRESS = "localhost";
    private static final int TEST_PORT = 12345;
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";

    @Mock
    StateManager mockedStateManager;
    @Mock
    ModbusListenerImpl mockedListener;

    ModbusTCPSlaveImpl modbusTcpSlaveImpl;

    @Test
    public void creationTest() throws Exception {

        PowerMockito.whenNew(ModbusListenerImpl.class).withArguments(Mockito.anyString(), Mockito.any(InetAddress.class),
                Mockito.any(StateManager.class)).thenReturn(mockedListener);

        // call test
        modbusTcpSlaveImpl = new ModbusTCPSlaveImpl(TEST_DEVICE_ID, TEST_ADDRESS, TEST_PORT, TEST_SLAVE_ADDRESS, mockedStateManager);

        // assertions
        verify(mockedListener, times(1)).addProcessImage(Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void openTest() throws Exception {
        // call test
        modbusTcpSlaveImpl = new ModbusTCPSlaveImpl(TEST_DEVICE_ID, TEST_ADDRESS, TEST_PORT, TEST_SLAVE_ADDRESS, mockedStateManager);
        modbusTcpSlaveImpl.open();

        // assertions
        boolean isRunning = (boolean) Whitebox.getInternalState(modbusTcpSlaveImpl, "isRunning");
        Assert.assertTrue(isRunning);
    }

    @Test
    public void closeTest() throws Exception {
        // call test
        modbusTcpSlaveImpl = new ModbusTCPSlaveImpl(TEST_DEVICE_ID, TEST_ADDRESS, TEST_PORT, TEST_SLAVE_ADDRESS, mockedStateManager);
        modbusTcpSlaveImpl.open();
        modbusTcpSlaveImpl.close();

        // assertions
        boolean isRunning = (boolean) Whitebox.getInternalState(modbusTcpSlaveImpl, "isRunning");
        Assert.assertFalse(isRunning);
    }

}
