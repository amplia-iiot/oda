package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.hardware.modbus.ModbusType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModbusDatastreamsFactoryImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_DATA_ADDRESS = 5;
    private static final String TEST_DEVICE_ID = "deviceId";

    private static final int TEST_REGISTERS_TO_READ = 1;
    private static final boolean TEST_READ_FROM_CACHE = false;
    private static final Type TEST_DATASTREAM_TYPE = String.class;
    private static final Map<String, Integer> TEST_MAPPER = new HashMap<>();
    static {
        TEST_MAPPER.put(TEST_DEVICE_ID, TEST_DATA_ADDRESS) ;
    }
    private static final ModbusType TEST_DATA_TYPE = ModbusType.HOLDING_REGISTER;



    private ModbusDatastreamsFactoryImpl testFactory;
    @Mock
    private ModbusMaster mockedModbusMaster;
    @Mock
    private ModbusConnectionsFinder mockedConnectionsFinder;

    private MockedConstruction<ModbusTypeToJavaTypeConverter> modbusTypeConverterCons;
    private MockedConstruction<JavaTypeToModbusTypeConverter> javaTypeConverterCons;
    private MockedConstruction<ModbusReadOperatorProcessor> readOperatorProcessorCons;
    private MockedConstruction<ModbusWriteOperatorProcessor> writeOperatorProcessorCons;
    private final List<List<?>> readOperatorProcessorArgs = new ArrayList<>();
    private final List<List<?>> writeOperatorProcessorArgs = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        modbusTypeConverterCons = mockConstruction(ModbusTypeToJavaTypeConverter.class);
        javaTypeConverterCons = mockConstruction(JavaTypeToModbusTypeConverter.class);
        readOperatorProcessorCons = mockConstruction(ModbusReadOperatorProcessor.class,
                (mock, mctx) -> readOperatorProcessorArgs.add(new ArrayList<>(mctx.arguments())));
        writeOperatorProcessorCons = mockConstruction(ModbusWriteOperatorProcessor.class,
                (mock, mctx) -> writeOperatorProcessorArgs.add(new ArrayList<>(mctx.arguments())));
        when(mockedConnectionsFinder.getModbusConnectionWithId(anyString())).thenReturn(mockedModbusMaster);

        testFactory = new ModbusDatastreamsFactoryImpl(mockedConnectionsFinder);
    }

    @AfterEach
    public void tearDown() {
        writeOperatorProcessorCons.close();
        readOperatorProcessorCons.close();
        javaTypeConverterCons.close();
        modbusTypeConverterCons.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(1, modbusTypeConverterCons.constructed().size());
        assertEquals(1, javaTypeConverterCons.constructed().size());
        assertEquals(1, readOperatorProcessorCons.constructed().size());
        assertEquals(mockedConnectionsFinder, readOperatorProcessorArgs.get(0).get(0));
        assertEquals(modbusTypeConverterCons.constructed().get(0), readOperatorProcessorArgs.get(0).get(1));
        assertEquals(1, writeOperatorProcessorCons.constructed().size());
        assertEquals(mockedConnectionsFinder, writeOperatorProcessorArgs.get(0).get(0));
        assertEquals(javaTypeConverterCons.constructed().get(0), writeOperatorProcessorArgs.get(0).get(1));
    }

    @Test
    public void testCreateModbusDatastreamsGetter() throws Exception {
        List<List<?>> getterArgs = new ArrayList<>();
        try (MockedConstruction<ModbusDatastreamsGetter> getterCons = mockConstruction(ModbusDatastreamsGetter.class,
                (mock, mctx) -> getterArgs.add(new ArrayList<>(mctx.arguments())))) {

            ModbusDatastreamsGetter result =
                    testFactory.createModbusDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DATASTREAM_TYPE, TEST_MAPPER,
                            TEST_DATA_TYPE, TEST_DATA_ADDRESS, TEST_READ_FROM_CACHE, TEST_REGISTERS_TO_READ);

            assertEquals(1, getterCons.constructed().size());
            assertEquals(getterCons.constructed().get(0), result);
            assertEquals(TEST_DATASTREAM_ID, getterArgs.get(0).get(0));
            assertEquals(TEST_DATASTREAM_TYPE, getterArgs.get(0).get(1));
            assertEquals(TEST_MAPPER, getterArgs.get(0).get(2));
            assertEquals(TEST_DATA_TYPE, getterArgs.get(0).get(3));
            assertEquals(TEST_DATA_ADDRESS, getterArgs.get(0).get(4));
            assertEquals(TEST_READ_FROM_CACHE, getterArgs.get(0).get(5));
            assertEquals(TEST_REGISTERS_TO_READ, getterArgs.get(0).get(6));
            assertEquals(readOperatorProcessorCons.constructed().get(0), getterArgs.get(0).get(7));
        }
    }

    @Test
    public void testCreateModbusDatastreamsSetter() throws Exception {
        List<List<?>> setterArgs = new ArrayList<>();
        try (MockedConstruction<ModbusDatastreamsSetter> setterCons = mockConstruction(ModbusDatastreamsSetter.class,
                (mock, mctx) -> setterArgs.add(new ArrayList<>(mctx.arguments())))) {

            ModbusDatastreamsSetter result =
                    testFactory.createModbusDatastreamsSetter(TEST_DATASTREAM_ID, TEST_DATASTREAM_TYPE, TEST_MAPPER,
                            TEST_DATA_TYPE, TEST_DATA_ADDRESS);

            assertEquals(1, setterCons.constructed().size());
            assertEquals(setterCons.constructed().get(0), result);
            assertEquals(TEST_DATASTREAM_ID, setterArgs.get(0).get(0));
            assertEquals(TEST_DATASTREAM_TYPE, setterArgs.get(0).get(1));
            assertEquals(TEST_MAPPER, setterArgs.get(0).get(2));
            assertEquals(TEST_DATA_TYPE, setterArgs.get(0).get(3));
            assertEquals(TEST_DATA_ADDRESS, setterArgs.get(0).get(4));
            assertEquals(writeOperatorProcessorCons.constructed().get(0), setterArgs.get(0).get(5));
        }
    }
}
