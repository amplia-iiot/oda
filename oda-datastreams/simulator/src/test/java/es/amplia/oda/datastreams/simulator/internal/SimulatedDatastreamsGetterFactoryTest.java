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
    private static final String TEST_FEED = "testFeed";
    private static final Object TEST_VALUE = "Hello World!";
    private static final double TEST_MIN_VALUE = 100.0;
    private static final double TEST_MAX_VALUE = 1000.0;
    private static final double TEST_MAX_DIFF = 10.0;


    private final SimulatedDatastreamsGetterFactory testFactory = new SimulatedDatastreamsGetterFactory();

    @Test
    public void createConstantDatastreamsGetter() throws Exception {
        PowerMockito.whenNew(ConstantDatastreamsGetter.class).withAnyArguments().thenReturn(null);

        testFactory.createConstantDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_VALUE);

        PowerMockito.verifyNew(ConstantDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM_ID),
                eq(TEST_DEVICE_ID), eq(TEST_FEED), eq(TEST_VALUE));
    }

    @Test
    public void createRandomDatastreamsGetter() throws Exception {
        PowerMockito.whenNew(RandomDatastreamsGetter.class).withAnyArguments().thenReturn(null);

        testFactory.createRandomDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_MIN_VALUE, TEST_MAX_VALUE,
                TEST_MAX_DIFF);

        PowerMockito.verifyNew(RandomDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM_ID),
                eq(TEST_DEVICE_ID), eq(TEST_FEED), eq(TEST_MIN_VALUE), eq(TEST_MAX_VALUE), eq(TEST_MAX_DIFF));
    }
}