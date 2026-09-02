package es.amplia.oda.datastreams.simulator.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        List<List<?>> constantGetterArgs = new ArrayList<>();
        try (MockedConstruction<ConstantDatastreamsGetter> constantGetterCons =
                     mockConstruction(ConstantDatastreamsGetter.class,
                             (mock, mctx) -> constantGetterArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createConstantDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_VALUE);

            assertEquals(1, constantGetterCons.constructed().size());
            assertEquals(Arrays.asList(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_VALUE),
                    constantGetterArgs.get(0));
        }
    }

    @Test
    public void createRandomDatastreamsGetter() throws Exception {
        List<List<?>> randomGetterArgs = new ArrayList<>();
        try (MockedConstruction<RandomDatastreamsGetter> randomGetterCons =
                     mockConstruction(RandomDatastreamsGetter.class,
                             (mock, mctx) -> randomGetterArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createRandomDatastreamsGetter(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_MIN_VALUE, TEST_MAX_VALUE,
                    TEST_MAX_DIFF);

            assertEquals(1, randomGetterCons.constructed().size());
            assertEquals(Arrays.asList(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_FEED, TEST_MIN_VALUE, TEST_MAX_VALUE,
                    TEST_MAX_DIFF), randomGetterArgs.get(0));
        }
    }
}
