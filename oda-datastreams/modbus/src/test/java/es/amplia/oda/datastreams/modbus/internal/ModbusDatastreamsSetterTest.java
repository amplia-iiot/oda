package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.hardware.modbus.ModbusType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ModbusDatastreamsSetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final Type TEST_DATASTREAM_TYPE = String.class;
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final int TEST_SLAVE_ADDRESS = 2;
    private static final Map<String, Integer> TEST_MAPPER =
            Collections.singletonMap(TEST_DEVICE_ID, TEST_SLAVE_ADDRESS);
    private static final ModbusType TEST_DATA_TYPE = ModbusType.INPUT_REGISTER;
    private static final int TEST_DATA_ADDRESS = 5;
    private static final String TEST_VALUE = "test";

    @Mock
    private ModbusWriteOperatorProcessor mockedWriterOperatorProcessor;

    private ModbusDatastreamsSetter testSetter;

    @BeforeEach
    public void setUp() {
        testSetter = new ModbusDatastreamsSetter(TEST_DATASTREAM_ID, TEST_DATASTREAM_TYPE, TEST_MAPPER, TEST_DATA_TYPE,
                TEST_DATA_ADDRESS, mockedWriterOperatorProcessor);
    }

    @Test
    public void getDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testSetter.getDatastreamIdSatisfied());
    }

    @Test
    public void getDatastreamType() {
        assertEquals(TEST_DATASTREAM_TYPE, testSetter.getDatastreamType());
    }

    @Test
    public void getDevicesIdManaged() {
        assertEquals(new ArrayList<>(TEST_MAPPER.keySet()), testSetter.getDevicesIdManaged());
    }

    @Test
    public void set() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = testSetter.set(TEST_DEVICE_ID, TEST_VALUE);
        future.get();

        verify(mockedWriterOperatorProcessor).write(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_TYPE), eq(TEST_DATA_TYPE),
                eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS), eq(TEST_VALUE));
    }

    @Test
    public void setWithUnknownDeviceId() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = testSetter.set("unknown", TEST_VALUE);
        assertThrows(ExecutionException.class, () -> future.get());
    }
}