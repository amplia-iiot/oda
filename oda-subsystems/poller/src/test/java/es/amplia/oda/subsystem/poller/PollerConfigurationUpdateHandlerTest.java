package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static es.amplia.oda.core.commons.utils.DevicePattern.NullDevicePattern;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PollerConfigurationUpdateHandlerTest {

    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    @Mock
    private ScheduledExecutorService mockedExecutor;
    @Mock
    private Poller mockedPoller;
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
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(3L,3L,NullDevicePattern)));
    }

    @Test
    public void twoPropertiesWithSameValuesAreMappedToOneEntryWithTwoValuesInEachMap() {
        properties.put("id1", "3");
        properties.put("id2", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(1, currentConfiguration.size());
        assertEquals(asSet("id1","id2"), currentConfiguration.get(new PollConfiguration(3L,3L,NullDevicePattern)));
    }

    @Test
    public void propertiesWithDifferentValuesAreMappedToDifferentEntries() {
        properties.put("id1", "3");
        properties.put("id2", "4");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(3L,3L,NullDevicePattern)));
        assertEquals(asSet("id2"), currentConfiguration.get(new PollConfiguration(4L,4L,NullDevicePattern)));
    }

    @Test
    public void propertiesWithDifferentFirstPollValueAreMappedToDifferentEntries() {
        properties.put("id1", "3;3");
        properties.put("id2", "4;3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(3L,3L,NullDevicePattern)));
        assertEquals(asSet("id2"), currentConfiguration.get(new PollConfiguration(4L,3L,NullDevicePattern)));
    }

    @Test
    public void propertiesWithDifferentSecondsBetweenPollsValueAreMappedToDifferentEntries() {
        properties.put("id1", "3;3");
        properties.put("id2", "3;4");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(3L,3L,NullDevicePattern)));
        assertEquals(asSet("id2"), currentConfiguration.get(new PollConfiguration(3L,4L,NullDevicePattern)));
    }

    @Test
    public void differentDeviceIdMappersAreMappedToDifferentEntries() {
        properties.put("id1;dev*", "3");
        properties.put("id1;sect*", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(3L, 3L, new DevicePattern("dev*"))));
        assertEquals(asSet("id1"), currentConfiguration.get(new PollConfiguration(3L, 3L, new DevicePattern("sect*"))));
    }

    @Test
    public void sameDeviceIdMappersAreMappedToSameEntries() {
        properties.put("id1;dev*", "3");
        properties.put("id2;dev*", "3");

        testConfigHandler.loadConfiguration(properties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(1, currentConfiguration.size());
        assertEquals(asSet("id1","id2"), currentConfiguration.get(new PollConfiguration(3L, 3L,new DevicePattern("dev*"))));
    }

    @Test
    public void testLoadDefaultConfiguration() {
        currentConfiguration = getCurrentConfiguration();
        currentConfiguration.put(new PollConfiguration(30, 30, DevicePattern.AllDevicePattern), Collections.singleton("test1"));
        currentConfiguration.put(new PollConfiguration(60, 10, DevicePattern.AllDevicePattern), Collections.singleton("test2"));
        currentConfiguration.put(new PollConfiguration(100, 100, DevicePattern.AllDevicePattern), Collections.singleton("test3"));

        testConfigHandler.loadDefaultConfiguration();

        assertTrue(currentConfiguration.isEmpty());
    }

    @Test
    public void applyConfiguration() {
        ScheduledFuture mockedFuture = mock(ScheduledFuture.class);
        List<ScheduledFuture> configuredTasks = new ArrayList<>();
        configuredTasks.add(mockedFuture);
        PollConfiguration pollConfiguration = new PollConfiguration(3L, 4L, DevicePattern.NullDevicePattern);
        Set<String> ids = Collections.singleton("id1");
        currentConfiguration = Collections.singletonMap(pollConfiguration, ids);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        Whitebox.setInternalState(testConfigHandler, "configuredTasks", configuredTasks);
        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", currentConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedFuture).cancel(eq(false));
        verify(mockedExecutor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(3L), eq(4L), eq(TimeUnit.SECONDS));
        Runnable poll = runnableCaptor.getValue();
        poll.run();
        verify(mockedPoller).runFor(eq(DevicePattern.NullDevicePattern), eq(ids));
        assertEquals(1, configuredTasks.size());
    }
}