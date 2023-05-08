package es.amplia.oda.datastreams.iec104.internal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class Iec104DatastreamsSetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final Type TEST_DATASTREAM_TYPE = Object.class;
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_VALUE = "test";

    @Mock
    private Iec104WriteOperatorProcessor mockedWriterOperatorProcessor;

    private Iec104DatastreamsSetter testSetter;

    @Before
    public void setUp() {
        testSetter = new Iec104DatastreamsSetter(TEST_DATASTREAM_ID, Arrays.asList(TEST_DEVICE_ID), mockedWriterOperatorProcessor);
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
        assertEquals(Arrays.asList(TEST_DEVICE_ID), testSetter.getDevicesIdManaged());
    }

    @Test
    public void set() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = testSetter.set(TEST_DEVICE_ID, TEST_VALUE);
        future.get();

        verify(mockedWriterOperatorProcessor).write(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID), eq(TEST_VALUE));
    }

}