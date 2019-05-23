package es.amplia.oda.dispatcher.opengate;

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

import static es.amplia.oda.dispatcher.opengate.DispatcherConfigurationUpdateHandler.REDUCE_BANDWIDTH_PROPERTY_NAME;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherConfigurationUpdateHandlerTest {

    @Mock
    private ScheduledExecutorService mockedExecutor;
    @Mock
    private OpenGateEventDispatcher mockedEventDispatcher;
    @Mock
    private Scheduler mockedScheduler;
    @InjectMocks
    private DispatcherConfigurationUpdateHandler testConfigHandler;

    private Map<DispatchConfiguration, Set<String>> currentConfiguration;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        String testDatastreamId1 = "test1";
        String testDatastreamId2 = "test2";
        String testDatastreamId3 = "test3";
        testProperties.put(testDatastreamId1, "30;30");
        testProperties.put(testDatastreamId2, "30");
        testProperties.put(testDatastreamId3, "10");
        testProperties.put(REDUCE_BANDWIDTH_PROPERTY_NAME, Boolean.TRUE.toString());

        testConfigHandler.loadConfiguration(testProperties);

        assertEquals(Boolean.TRUE, Whitebox.getInternalState(testConfigHandler, "reduceBandwidthMode"));
        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(new HashSet<>(Arrays.asList(testDatastreamId1, testDatastreamId2)),
                currentConfiguration.get(new DispatchConfiguration(30,30)));
        assertEquals(Collections.singleton(testDatastreamId3),
                currentConfiguration.get(new DispatchConfiguration(10,10)));
    }

    @Test
    public void testLoadConfigurationInvalidConfigurationIsIgnored() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put("testProperty", "ignored");

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test
    public void testLoadDefaultConfiguration() {
        currentConfiguration = getCurrentConfiguration();
        currentConfiguration.put(new DispatchConfiguration(30, 30), Collections.singleton("test1"));
        currentConfiguration.put(new DispatchConfiguration(10, 60), Collections.singleton("test2"));
        currentConfiguration.put(new DispatchConfiguration(10, 10), Collections.singleton("test3"));

        testConfigHandler.loadDefaultConfiguration();

        assertTrue(currentConfiguration.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<DispatchConfiguration, Set<String>> getCurrentConfiguration() {
        return (Map<DispatchConfiguration, Set<String>>) Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
    }


    @Test
    public void testApplyConfiguration() {
        currentConfiguration = new HashMap<>();
        Set<String> set1 = new HashSet<>(Arrays.asList("test1", "test2"));
        Set<String> set2 = new HashSet<>(Collections.singletonList("test3"));
        currentConfiguration.put(new DispatchConfiguration(30,30), set1);
        currentConfiguration.put(new DispatchConfiguration(60,10), set2);
        ScheduledFuture mockedTask1 = mock(ScheduledFuture.class);
        ScheduledFuture mockedTask2 = mock(ScheduledFuture.class);
        ScheduledFuture mockedTask3 = mock(ScheduledFuture.class);
        List<ScheduledFuture> mockedTasks = new ArrayList<>();
        mockedTasks.add(mockedTask1);
        mockedTasks.add(mockedTask2);
        mockedTasks.add(mockedTask3);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", currentConfiguration);
        Whitebox.setInternalState(testConfigHandler, "configuredTasks", mockedTasks);
        Whitebox.setInternalState(testConfigHandler, "reduceBandwidthMode", true);

        testConfigHandler.applyConfiguration();

        verify(mockedTask1).cancel(eq(false));
        verify(mockedTask2).cancel(eq(false));
        verify(mockedTask3).cancel(eq(false));
        verify(mockedEventDispatcher).setReduceBandwidthMode(eq(true));
        verify(mockedEventDispatcher).setDatastreamIdsConfigured(eq(currentConfiguration.values()));
        verify(mockedExecutor).scheduleAtFixedRate(runnableCaptor.capture(), eq(30L), eq(30L), eq(TimeUnit.SECONDS));
        runnableCaptor.getValue().run();
        verify(mockedScheduler).runFor(eq(set1));
        verify(mockedExecutor).scheduleAtFixedRate(runnableCaptor.capture(), eq(60L), eq(10L), eq(TimeUnit.SECONDS));
        runnableCaptor.getValue().run();
        verify(mockedScheduler).runFor(eq(set2));
        assertEquals(2, mockedTasks.size());
    }
}