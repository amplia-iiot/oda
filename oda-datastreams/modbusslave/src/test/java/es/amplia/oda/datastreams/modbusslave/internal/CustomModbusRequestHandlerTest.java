package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import es.amplia.oda.core.commons.interfaces.StateManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        Mockito.when(mockedModbusTransport.readRequest(mockedModbusListener)).thenReturn(mockedModbusRequest);

        // call method
        modbusRequestHandler.handleRequest(mockedModbusTransport, mockedModbusListener);

        // assertions
        Mockito.verify(mockedModbusTransport).writeMessage(Mockito.any());
        Map<Integer, ProcessImage> processImages = (Map<Integer, ProcessImage>)
                Whitebox.getInternalState(modbusRequestHandler, "processImages");
        Assertions.assertNotNull(processImages.get(TEST_SLAVE_ADDRESS));
        Mockito.verify(mockedStateManager).onReceivedEvents(Mockito.anyList());
    }
}
