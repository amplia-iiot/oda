package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
public class PollerConfigurationUpdateHandlerTest {

    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    @Mock
    private Poller mockedPoller;
    @Mock
    private Scheduler mockedScheduler;
    @InjectMocks
    private PollerConfigurationUpdateHandler testConfigHandler;

    private final Dictionary<String, String> properties = new Hashtable<>();
    private Map<PollConfiguration, Set<String>> currentConfiguration;

    @Test
    public void parsingEmptyDictionaryLeavesResultEmpty() {
        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertTrue(currentConfiguration.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<PollConfiguration, Set<String>> getCurrentConfiguration() {
        return (Map<PollConfiguration, Set<String>>)
                Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
    }

    @Test
    public void onePropertyIsMappedToOneEntryWithOneValueInEachMap() {
        properties.put("id1", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(1, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 3L)));
    }

    @Test
    public void twoPropertiesWithSameValuesAreMappedToOneEntryWithTwoValuesInEachMap() {
        properties.put("id1", "3");
        properties.put("id2", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(1, currentConfiguration.size());
        assertEquals(asSet("id1","id2"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 3L)));
    }

    @Test
    public void propertiesWithDifferentValuesAreMappedToDifferentEntries() {
        properties.put("id1", "3");
        properties.put("id2", "4");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 3L)));
        assertEquals(asSet("id2"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 4L, 4L)));
    }

    @Test
    public void propertiesWithDifferentFirstPollValueAreMappedToDifferentEntries() {
        properties.put("id1", "3;3");
        properties.put("id2", "4;3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 3L)));
        assertEquals(asSet("id2"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 4L, 3L)));
    }

    @Test
    public void propertiesWithDifferentSecondsBetweenPollsValueAreMappedToDifferentEntries() {
        properties.put("id1", "3;3");
        properties.put("id2", "3;4");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 3L)));
        assertEquals(asSet("id2"), currentConfiguration.get(new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 4L)));
    }

    @Test
    public void differentDeviceIdMappersAreMappedToDifferentEntries() {
        properties.put("id1;dev*", "3");
        properties.put("id1;sect*", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(new DevicePattern("dev*"), 3L, 3L)));
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(new DevicePattern("sect*"), 3L, 3L)));
    }

    @Test
    public void sameDeviceIdMappersAreMappedToSameEntries() {
        properties.put("id1;dev*", "3");
        properties.put("id2;dev*", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(1, currentConfiguration.size());
        assertEquals(asSet("id1","id2"), currentConfiguration.get(new PollConfiguration(new DevicePattern("dev*"), 3L, 3L)));
    }

    @Test
    public void testLoadDefaultConfiguration() {
        currentConfiguration = getCurrentConfiguration();
        currentConfiguration.put(new PollConfiguration(DevicePattern.AllDevicePattern, 30, 30), Collections.singleton("test1"));
        currentConfiguration.put(new PollConfiguration(DevicePattern.AllDevicePattern, 60, 10), Collections.singleton("test2"));
        currentConfiguration.put(new PollConfiguration(DevicePattern.AllDevicePattern, 100, 100), Collections.singleton("test3"));

        testConfigHandler.loadDefaultConfiguration();

        assertTrue(currentConfiguration.isEmpty());
    }

    @Test
    public void applyConfiguration() {
        PollConfiguration pollConfiguration = new PollConfiguration(DevicePattern.NullDevicePattern, 3L, 4L);
        Set<String> ids = Collections.singleton("id1");
        currentConfiguration = Collections.singletonMap(pollConfiguration, ids);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", currentConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedScheduler).clear();
        verify(mockedScheduler).schedule(runnableCaptor.capture(), eq(3L), eq(4L), eq(TimeUnit.SECONDS));
        Runnable poll = runnableCaptor.getValue();
        poll.run();
        verify(mockedPoller).poll(eq(DevicePattern.NullDevicePattern), eq(ids));
    }
}