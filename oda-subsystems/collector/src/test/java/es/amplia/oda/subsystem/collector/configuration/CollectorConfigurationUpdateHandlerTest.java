package es.amplia.oda.subsystem.collector.configuration;

import es.amplia.oda.subsystem.collector.Collector;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Scheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CollectorConfigurationUpdateHandlerTest {

    private static final String TEST_STRING_DEVICE_PATTERN = "testDevice";
    private static final DevicePattern TEST_DEVICE_PATTERN = new DevicePattern(TEST_STRING_DEVICE_PATTERN);
    private static final String TEST_DATASTREAM = "testDatastream";
    private static final long TEST_DELAY = 5;
    private static final long TEST_PERIOD = 30;
    private static final String TEST_DATASTREAM_2 = "testDatastream2";
    private static final String TEST_DATASTREAM_3 = "testDatastream2";
    private static final long TEST_DELAY_2 = 10;
    private static final long TEST_PERIOD_2 = 20;
    private static final DevicePattern TEST_DEVICE_PATTERN_2 = new DevicePattern("*");
    private static final CollectorConfiguration TEST_CONF_1 =
            new CollectorConfiguration(TEST_DEVICE_PATTERN, TEST_DATASTREAM, TEST_DELAY, TEST_PERIOD);
    private static final CollectorConfiguration TEST_CONF_2 =
            new CollectorConfiguration(TEST_DEVICE_PATTERN, TEST_DATASTREAM_2, TEST_DELAY, TEST_PERIOD);
    private static final CollectorConfiguration TEST_CONF_3 =
            new CollectorConfiguration(TEST_DEVICE_PATTERN, TEST_DATASTREAM_3, TEST_DELAY_2, TEST_PERIOD_2);
    private static final CollectorConfiguration TEST_CONF_4 =
            new CollectorConfiguration(TEST_DEVICE_PATTERN_2, TEST_DATASTREAM, TEST_DELAY, TEST_PERIOD_2);
    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";

    @Mock
    private Collector mockedCollector;
    @Mock
    private Scheduler mockedScheduler;
    @InjectMocks
    private CollectorConfigurationUpdateHandler testHandler;

    @Captor
    private ArgumentCaptor<Runnable> taskCaptor;

    private final List<CollectorConfiguration> testConfigurations = new ArrayList<>();


    @Before
    public void setUp() {
        testConfigurations.add(TEST_CONF_1);
        testConfigurations.add(TEST_CONF_2);
        testConfigurations.add(TEST_CONF_3);
        testConfigurations.add(TEST_CONF_4);
    }

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> testProps = new Hashtable<>();
        testProps.put(TEST_DATASTREAM + ";" + TEST_STRING_DEVICE_PATTERN, TEST_DELAY + ";" + TEST_PERIOD);
        testProps.put(TEST_DATASTREAM_2, Long.toString(TEST_PERIOD_2));
        testProps.put("invalid", "invalid");

        testHandler.loadConfiguration(testProps);

        verify(mockedScheduler).clear();
        List<CollectorConfiguration> conf = getCurrentConfiguration();
        assertEquals(2, conf.size());
        assertConfigurationExists(TEST_DEVICE_PATTERN, TEST_DATASTREAM, TEST_DELAY, TEST_PERIOD, conf);
        assertConfigurationExists(DevicePattern.NullDevicePattern, TEST_DATASTREAM_2, TEST_PERIOD_2, TEST_PERIOD_2, conf);
    }

    private void assertConfigurationExists(DevicePattern devicePattern, String datastream, long delay, long period,
                                           List<CollectorConfiguration> configurations) {
        if (configurations.stream().noneMatch(conf -> devicePattern.equals(conf.getDevicePattern()) &&
                datastream.equals(conf.getDatastream()) && delay == conf.getDelay() && period == conf.getPeriod())) {
            fail("No configuration found with device pattern " + devicePattern + ", datastream " + datastream +
                            ", delay " + delay + ", period " + period);
        }
    }

    @SuppressWarnings("unchecked")
    private List<CollectorConfiguration> getCurrentConfiguration() {
        return (List<CollectorConfiguration>) Whitebox.getInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME);
    }

    @Test
    public void testLoadDefaultConfiguration() {
        Whitebox.setInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME, testConfigurations);

        testHandler.loadDefaultConfiguration();

        verify(mockedScheduler).clear();
        assertTrue(testConfigurations.isEmpty());
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME, testConfigurations);

        testHandler.applyConfiguration();

        verify(mockedScheduler).schedule(taskCaptor.capture(), eq(TEST_DELAY), eq(TEST_PERIOD), eq(TimeUnit.SECONDS));
        Runnable runnable = taskCaptor.getValue();
        runnable.run();
        verify(mockedCollector)
                .collect(eq(TEST_DEVICE_PATTERN), eq(new HashSet<>(Arrays.asList(TEST_DATASTREAM, TEST_DATASTREAM_2))));
        verify(mockedScheduler).schedule(taskCaptor.capture(), eq(TEST_DELAY_2), eq(TEST_PERIOD_2), eq(TimeUnit.SECONDS));
        runnable = taskCaptor.getValue();
        runnable.run();
        verify(mockedCollector).collect(eq(TEST_DEVICE_PATTERN), eq(Collections.singleton(TEST_DATASTREAM_3)));
        verify(mockedScheduler).schedule(taskCaptor.capture(), eq(TEST_DELAY), eq(TEST_PERIOD_2), eq(TimeUnit.SECONDS));
        runnable = taskCaptor.getValue();
        runnable.run();
        verify(mockedCollector).collect(eq(TEST_DEVICE_PATTERN_2), eq(Collections.singleton(TEST_DATASTREAM)));
    }
}