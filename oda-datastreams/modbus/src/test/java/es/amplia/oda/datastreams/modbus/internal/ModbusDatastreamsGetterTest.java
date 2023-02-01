package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.datastreams.modbus.ModbusType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ModbusDatastreamsGetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final Type TEST_DATASTREAM_TYPE = String.class;
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final int TEST_SLAVE_ADDRESS = 2;
    private static final Map<String, Integer> TEST_MAPPER = Collections.singletonMap(TEST_DEVICE_ID, TEST_SLAVE_ADDRESS);
    private static final ModbusType TEST_DATA_TYPE = ModbusType.INPUT_REGISTER;
    private static final int TEST_DATA_ADDRESS = 5;

    @Mock
    private ModbusReadOperatorProcessor mockedReadOperatorProcessor;

    private ModbusDatastreamsGetter testGetter;

    @Before
    public void setUp() {
        testGetter = new ModbusDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DATASTREAM_TYPE, TEST_MAPPER, TEST_DATA_TYPE,
                TEST_DATA_ADDRESS, mockedReadOperatorProcessor);
    }

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testGetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testGetDevicesIdManaged() {
        assertEquals(new ArrayList<>(TEST_MAPPER.keySet()), testGetter.getDevicesIdManaged());
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        future.get();

        verify(mockedReadOperatorProcessor)
                .read(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_TYPE), eq(TEST_DATA_TYPE), eq(TEST_SLAVE_ADDRESS), eq(TEST_DATA_ADDRESS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUnknownDevice() throws ExecutionException, InterruptedException {
        CompletableFuture<CollectedValue> future = testGetter.get("unknown");
        future.get();
    }
}