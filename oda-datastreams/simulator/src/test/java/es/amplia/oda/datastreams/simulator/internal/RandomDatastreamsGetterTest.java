package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class RandomDatastreamsGetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final double TEST_MIN_VALUE = 500;
    private static final double TEST_MAX_VALUE = 1000;
    private static final double TEST_MAX_DIFFERENCE = 10;

    private final RandomDatastreamsGetter testGetter =
            new RandomDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_MIN_VALUE, TEST_MAX_VALUE,
                    TEST_MAX_DIFFERENCE);

    @Test
    public void testConstructor() {
        verifyValue(getInternalLastValue());
    }

    private double getInternalLastValue() {
        return (double) Whitebox.getInternalState(testGetter, "lastValue");
    }

    private void verifyValue(double value) {
        assertTrue(value >= TEST_MIN_VALUE);
        assertTrue(value <= TEST_MAX_VALUE);
    }

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
        double lastValue = getInternalLastValue();

        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        CollectedValue collectedValue = future.get();
        double nextValue = (double) collectedValue.getValue();

        verifyValue(nextValue);
        // Nearly impossible to get the same value
        assertNotEquals(nextValue, lastValue);
        assertTrue(Math.abs(nextValue - lastValue) <= TEST_MAX_DIFFERENCE / 100 * (TEST_MAX_VALUE - TEST_MIN_VALUE));
    }

    @Test
    public void testGetWithLastValueNearMinValue() throws ExecutionException, InterruptedException {
        double lastValue = TEST_MIN_VALUE + 1;
        Whitebox.setInternalState(testGetter, "lastValue", lastValue);

        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        CollectedValue collectedValue = future.get();
        double nextValue = (double) collectedValue.getValue();

        verifyValue(nextValue);
        // Nearly impossible to get the same value
        assertNotEquals(nextValue, lastValue);
        assertTrue(Math.abs(nextValue - lastValue) <= TEST_MAX_DIFFERENCE / 100 * (TEST_MAX_VALUE - TEST_MIN_VALUE));
    }



    @Test
    public void testGetWithLastValueNearMaxValue() throws ExecutionException, InterruptedException {
        double lastValue = TEST_MAX_VALUE - 1;
        Whitebox.setInternalState(testGetter, "lastValue", lastValue);

        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        CollectedValue collectedValue = future.get();
        double nextValue = (double) collectedValue.getValue();

        verifyValue(nextValue);
        // Nearly impossible to get the same value
        assertNotEquals(nextValue, lastValue);
        assertTrue(Math.abs(nextValue - lastValue) <= TEST_MAX_DIFFERENCE / 100 * (TEST_MAX_VALUE - TEST_MIN_VALUE));
    }

    @Test
    public void testGetWithLastValueInTheMiddle() throws ExecutionException, InterruptedException {
        double lastValue = TEST_MIN_VALUE + (TEST_MAX_VALUE - TEST_MIN_VALUE) / 2;
        Whitebox.setInternalState(testGetter, "lastValue", lastValue);

        CompletableFuture<CollectedValue> future = testGetter.get(TEST_DEVICE_ID);
        CollectedValue collectedValue = future.get();
        double nextValue = (double) collectedValue.getValue();

        verifyValue(nextValue);
        // Nearly impossible to get the same value
        assertNotEquals(nextValue, lastValue);
        assertTrue(Math.abs(nextValue - lastValue) <= TEST_MAX_DIFFERENCE / 100 * (TEST_MAX_VALUE - TEST_MIN_VALUE));
    }
}