package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import es.amplia.oda.core.commons.interfaces.StateManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusListenerImpl.class)
public class ModbusListenerImplTest {

    private static final int TEST_PORT = 12345;
    private static final String TEST_DEVICE_ID = "deviceId";
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final InetAddress inetAddress = new InetSocketAddress(TEST_PORT).getAddress();

    @Mock
    StateManager mockedStateManager;
    @Mock
    AbstractModbusTransport mockedTransport;
    @Mock
    AbstractModbusListener mockedAbstractListener;
    @Mock
    ModbusRequest mockedModbusRequest;

    ModbusListenerImpl modbusListener;

    @Test
    public void processImageTest() {
        // call test
        modbusListener = new ModbusListenerImpl(TEST_DEVICE_ID, inetAddress, mockedStateManager);
        modbusListener.addProcessImage(TEST_SLAVE_ADDRESS, new SimpleProcessImage(TEST_SLAVE_ADDRESS));

        // assertions
        ProcessImage currentProcessImage = modbusListener.getProcessImage(TEST_SLAVE_ADDRESS);
        Assert.assertNotNull(currentProcessImage);
    }

    @Test
    public void handleRequestTest() throws ModbusIOException {
        PowerMockito.when(mockedTransport.readRequest(mockedAbstractListener)).thenReturn(mockedModbusRequest);

        // call test
        modbusListener = new ModbusListenerImpl(TEST_DEVICE_ID, inetAddress, mockedStateManager);
        modbusListener.addProcessImage(TEST_SLAVE_ADDRESS, new SimpleProcessImage(TEST_SLAVE_ADDRESS));
        modbusListener.handleRequest(mockedTransport, mockedAbstractListener);

        // assertions
        verify(mockedStateManager, times(1)).onReceivedEvents(Mockito.anyList());
    }
}
