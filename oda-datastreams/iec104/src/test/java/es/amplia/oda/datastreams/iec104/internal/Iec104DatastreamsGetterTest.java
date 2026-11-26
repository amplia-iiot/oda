package es.amplia.oda.datastreams.iec104.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class Iec104DatastreamsGetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";

    @Mock
    private Iec104ReadOperatorProcessor mockedReadOperatorProcessor;

    private Iec104DatastreamsGetter testGetter;

    @BeforeEach
    public void setUp() {
        testGetter = new Iec104DatastreamsGetter(TEST_DATASTREAM_ID, Arrays.asList(TEST_DEVICE_ID), mockedReadOperatorProcessor);
    }

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testGetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testGetDevicesIdManaged() {
        assertEquals(Arrays.asList(TEST_DEVICE_ID), testGetter.getDevicesIdManaged());
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        future.get();

        verify(mockedReadOperatorProcessor)
                .read(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
    }

}