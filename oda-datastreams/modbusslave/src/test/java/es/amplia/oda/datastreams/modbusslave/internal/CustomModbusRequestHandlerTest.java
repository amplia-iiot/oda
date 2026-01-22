package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import es.amplia.oda.core.commons.interfaces.StateManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CustomModbusRequestHandlerTest {

    private static final String TEST_DEVICE_IP = "1.2.3.4";
    private static final int TEST_SLAVE_ADDRESS = 1;
    private static final String TEST_DEVICE_ID = "deviceId";

    @Mock
    AbstractModbusTransport mockedModbusTransport;
    @Mock
    AbstractModbusListener mockedModbusListener;
    @Mock
    ModbusRequest mockedModbusRequest;
    @Mock
    private StateManager mockedStateManager;


    @Test
    public void handleRequestTest() throws ModbusIOException {
        // conditions
        CustomModbusRequestHandler modbusRequestHandler = new CustomModbusRequestHandler(TEST_DEVICE_ID, TEST_DEVICE_IP,
                TEST_SLAVE_ADDRESS, mockedStateManager);
        PowerMockito.when(mockedModbusTransport.readRequest(mockedModbusListener)).thenReturn(mockedModbusRequest);

        // call method
        modbusRequestHandler.handleRequest(mockedModbusTransport, mockedModbusListener);

        // assertions
        Mockito.verify(mockedModbusTransport).writeMessage(Mockito.any());
        Map<Integer, ProcessImage> processImages = (Map<Integer, ProcessImage>)
                Whitebox.getInternalState(modbusRequestHandler, "processImages");
        Assert.assertNotNull(processImages.get(TEST_SLAVE_ADDRESS));
        Mockito.verify(mockedStateManager).onReceivedEvents(Mockito.anyList());
    }
}
