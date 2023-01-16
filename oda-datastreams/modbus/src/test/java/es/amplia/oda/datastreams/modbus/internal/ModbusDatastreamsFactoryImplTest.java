package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsManager;
import es.amplia.oda.datastreams.modbus.ModbusType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModbusDatastreamsFactoryImpl.class)
public class ModbusDatastreamsFactoryImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_DATA_ADDRESS = 5;
    private static final String TEST_DEVICE_ID = "deviceId";
    private static final Type TEST_DATASTREAM_TYPE = String.class;
    private static final Map<String, Integer> TEST_MAPPER = new HashMap<>();
    static {
        TEST_MAPPER.put(TEST_DEVICE_ID, TEST_DATA_ADDRESS) ;
    }
    private static final ModbusType TEST_DATA_TYPE = ModbusType.HOLDING_REGISTER;

    @Mock
    private ModbusMaster mockedModbusMaster;
    private final List<ModbusMaster> mockedModbusMasterList = Collections.singletonList(mockedModbusMaster);
    private ModbusDatastreamsFactoryImpl testFactory;
    @Mock
    private ModbusConnectionsManager mockedConnectionsManager;
    @Mock
    private ModbusTypeToJavaTypeConverter mockedModbusTypeConverter;
    @Mock
    private JavaTypeToModbusTypeConverter mockedJavaTypeConverter;
    private ModbusReadOperatorProcessor mockedReadOperatorProcessor = new ModbusReadOperatorProcessor(mockedModbusMaster, mockedModbusTypeConverter);
    private final List<ModbusReadOperatorProcessor> mockedReadOperatorProcessors = Collections.singletonList(mockedReadOperatorProcessor);
    @Mock
    private ModbusWriteOperatorProcessor mockedWriteOperatorProcessor;
    @Mock
    private ModbusDatastreamsGetter mockedDatastreamsGetter;
    @Mock
    private ModbusDatastreamsSetter mockedDatastreamsSetter;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(ModbusTypeToJavaTypeConverter.class).withAnyArguments()
                .thenReturn(mockedModbusTypeConverter);
        PowerMockito.whenNew(JavaTypeToModbusTypeConverter.class).withAnyArguments()
                .thenReturn(mockedJavaTypeConverter);
        PowerMockito.whenNew(ModbusReadOperatorProcessor.class).withAnyArguments()
                .thenReturn(mockedReadOperatorProcessor);
        PowerMockito.whenNew(ModbusWriteOperatorProcessor.class).withAnyArguments()
                .thenReturn(mockedWriteOperatorProcessor);

        testFactory = new ModbusDatastreamsFactoryImpl(mockedConnectionsManager);
    }

    @Ignore
    @Test
    public void testCreateModbusDatastreamsGetter() throws Exception {
        PowerMockito.whenNew(ModbusDatastreamsGetter.class).withAnyArguments().thenReturn(mockedDatastreamsGetter);

        ModbusDatastreamsGetter result =
                testFactory.createModbusDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DATASTREAM_TYPE, TEST_MAPPER,
                        TEST_DATA_TYPE, TEST_DATA_ADDRESS);

        PowerMockito.when(mockedConnectionsManager.getModbusConnectionWithId(TEST_DEVICE_ID)).thenReturn(mockedModbusMaster);


        assertEquals(mockedDatastreamsGetter, result);
        PowerMockito.verifyNew(ModbusDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM_ID),
                eq(TEST_DATASTREAM_TYPE), eq(TEST_MAPPER), eq(TEST_DATA_TYPE), eq(TEST_DATA_ADDRESS),
                eq(mockedReadOperatorProcessors));
        PowerMockito.verifyNew(ModbusTypeToJavaTypeConverter.class).withNoArguments();
        PowerMockito.verifyNew(ModbusReadOperatorProcessor.class)
                .withArguments(eq(mockedModbusMaster), eq(mockedModbusTypeConverter));
    }

    @Ignore
    @Test
    public void testCreateModbusDatastreamsSetter() throws Exception {
        PowerMockito.whenNew(ModbusDatastreamsSetter.class).withAnyArguments().thenReturn(mockedDatastreamsSetter);

        ModbusDatastreamsSetter result =
                testFactory.createModbusDatastreamsSetter(TEST_DATASTREAM_ID, TEST_DATASTREAM_TYPE, TEST_MAPPER,
                        TEST_DATA_TYPE, TEST_DATA_ADDRESS);

        assertEquals(mockedDatastreamsSetter, result);
        PowerMockito.verifyNew(ModbusDatastreamsSetter.class).withArguments(eq(TEST_DATASTREAM_ID),
                eq(TEST_DATASTREAM_TYPE), eq(TEST_MAPPER), eq(TEST_DATA_TYPE), eq(TEST_DATA_ADDRESS),
                eq(mockedWriteOperatorProcessor));
        PowerMockito.verifyNew(JavaTypeToModbusTypeConverter.class).withNoArguments();
        PowerMockito.verifyNew(ModbusWriteOperatorProcessor.class)
                .withArguments(eq(mockedModbusMaster), eq(mockedJavaTypeConverter));
    }
}