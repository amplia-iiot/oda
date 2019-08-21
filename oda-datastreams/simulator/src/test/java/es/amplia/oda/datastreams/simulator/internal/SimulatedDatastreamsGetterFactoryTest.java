package es.amplia.oda.datastreams.simulator.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SimulatedDatastreamsGetterFactory.class)
public class SimulatedDatastreamsGetterFactoryTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final double TEST_MIN_VALUE = 100.0;
    private static final double TEST_MAX_VALUE = 1000.0;
    private static final double TEST_MAX_DIFF = 10.0;


    private final SimulatedDatastreamsGetterFactory testFactory = new SimulatedDatastreamsGetterFactory();


    @Test
    public void createSimulatedDatastreamsGetter() throws Exception {
        PowerMockito.whenNew(SimulatedDatastreamsGetter.class).withAnyArguments().thenReturn(null);

        testFactory.createSimulatedDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_MIN_VALUE, TEST_MAX_VALUE,
                TEST_MAX_DIFF);

        PowerMockito.verifyNew(SimulatedDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM_ID),
                eq(TEST_DEVICE_ID), eq(TEST_MIN_VALUE), eq(TEST_MAX_VALUE), eq(TEST_MAX_DIFF));
    }
}