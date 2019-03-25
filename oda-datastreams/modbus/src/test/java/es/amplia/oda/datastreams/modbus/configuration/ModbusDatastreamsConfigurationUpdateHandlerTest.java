package es.amplia.oda.datastreams.modbus.configuration;

import es.amplia.oda.datastreams.modbus.ModbusDatastreamsManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ModbusDatastreamsConfigurationUpdateHandlerTest {

    @Mock
    private ModbusDatastreamsManager mockedModbusDatastreamsManager;
    @InjectMocks
    private ModbusDatastreamsConfigurationUpdateHandler testConfigHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put("datastream1;device1", "datastreamType:boolean,slaveAddress:1,dataType:inputDiscrete,dataAddress:1");
        props.put("datastream2;device1", "datastreamType:Integer,slaveAddress:1,dataType:input_discrete,dataAddress:2");
        props.put("datastream3;device1", "datastreamType:bit,slaveAddress:1,dataType:coil,dataAddress:1");
        props.put("datastream4;device1", "datastreamType:double,slaveAddress:1,dataType:coil,dataAddress:1");
        props.put("datastream5;device1", "datastreamType:bool,slaveAddress:1,dataType:coil,dataAddress:1");
        props.put("datastream1;device2", "datastreamType:byteArray,slaveAddress:2,dataType:input_register,dataAddress:1");
        props.put("datastream2;device2", "datastreamType:bytes,slaveAddress:2,dataType:inputRegister,dataAddress:2");
        props.put("datastream3;device2", "datastreamType:short,slaveAddress:2,dataType:input_register,dataAddress:3");
        props.put("datastream4;device2", "datastreamType:int,slaveAddress:2,dataType:input_register,dataAddress:4");
        props.put("datastream5;device2", "datastreamType:float,slaveAddress:2,dataType:input_register,dataAddress:5");
        props.put("datastream6;device2", "datastreamType:long,slaveAddress:2,dataType:input_register,dataAddress:6");
        props.put("datastream7;device2", "datastreamType:double,slaveAddress:2,dataType:input_register,dataAddress:7");
        props.put("datastream8;device2", "datastreamType:boolean,slaveAddress:2,dataType:input_register,dataAddress:7");
        props.put("datastream1;device3", "datastreamType:byteArray,slaveAddress:2,dataType:holding_register,dataAddress:1");
        props.put("datastream2;device3", "datastreamType:bytes,slaveAddress:2,dataType:holdingRegister,dataAddress:2");
        props.put("datastream3;device3", "datastreamType:short,slaveAddress:2,dataType:holdingRegister,dataAddress:3");
        props.put("datastream4;device3", "datastreamType:int,slaveAddress:2,dataType:holdingRegister,dataAddress:4");
        props.put("datastream5;device3", "datastreamType:float,slaveAddress:2,dataType:holdingRegister,dataAddress:5");
        props.put("datastream6;device3", "datastreamType:long,slaveAddress:2,dataType:holdingRegister,dataAddress:6");
        props.put("datastream7;device3", "datastreamType:double,slaveAddress:2,dataType:holdingRegister,dataAddress:7");
        props.put("datastream8;device3", "datastreamType:bool,slaveAddress:2,dataType:holding_register,dataAddress:7");
        props.put("datastream9;device4", "datastreamType:bool,slaveAddress:invalid,dataType:holding_register,dataAddress:7");
        props.put("datastream10;device4", "datastreamType:bool,slaveAddress:2,dataType:holding_register,dataAddress:invalid");

        List<ModbusDatastreamsConfiguration> spiedConfiguration = spy(new ArrayList<>());

        Whitebox.setInternalState(testConfigHandler, "currentModbusDatastreamsConfigurations", spiedConfiguration);

        testConfigHandler.loadConfiguration(props);

        verify(spiedConfiguration).clear();
        verify(spiedConfiguration, times(15)).add(any(ModbusDatastreamsConfiguration.class));
    }

    @Test
    public void testLoadDefaultConfiguration() {
        List<ModbusDatastreamsConfiguration> spiedConfiguration = spy(new ArrayList<>());
        Whitebox.setInternalState(testConfigHandler, "currentModbusDatastreamsConfigurations", spiedConfiguration);

        testConfigHandler.loadDefaultConfiguration();

        verify(spiedConfiguration).clear();
    }

    @Test
    public void testApplyConfiguration() {
        List<ModbusDatastreamsConfiguration> currentConfiguration = new ArrayList<>();
        Whitebox.setInternalState(testConfigHandler, "currentModbusDatastreamsConfigurations", currentConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedModbusDatastreamsManager).loadConfiguration(eq(currentConfiguration));
    }
}