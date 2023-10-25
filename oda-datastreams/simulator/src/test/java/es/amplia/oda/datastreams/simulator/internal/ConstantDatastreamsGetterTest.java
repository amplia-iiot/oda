package es.amplia.oda.datastreams.simulator.internal;

import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.*;

import static org.junit.Assert.*;

public class ConstantDatastreamsGetterTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_FEED = "testFeed";
    private static final Object TEST_VALUE = true;

    private final ConstantDatastreamsGetter testGetter =
            new ConstantDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_VALUE);

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testGetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testGetDevicesIdManaged() {
        assertEquals(Collections.singletonList(TEST_DEVICE_ID), testGetter.getDevicesIdManaged());
    }

    @Test
    public void testGet() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();

        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        CollectedValue collectedValue = future.get();

        assertEquals(TEST_VALUE, collectedValue.getValue());
        assertTrue(beforeTest <= collectedValue.getAt());
        assertTrue(collectedValue.getAt() <= System.currentTimeMillis());
    }
}