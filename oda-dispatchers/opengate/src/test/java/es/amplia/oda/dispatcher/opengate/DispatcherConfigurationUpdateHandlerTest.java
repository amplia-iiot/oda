package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.event.api.EventDispatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.dispatcher.opengate.DispatcherConfigurationUpdateHandler.EVENT_CONTENT_TYPE_PROPERTY_NAME;
import static es.amplia.oda.dispatcher.opengate.DispatcherConfigurationUpdateHandler.REDUCED_OUTPUT_PROPERTY_NAME;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DispatcherConfigurationUpdateHandlerTest {

    @Mock
    private EventDispatcherFactory mockedFactory;
    @Mock
    private Scheduler mockedScheduler;
    @Mock
    private ServiceRegistrationManager<EventDispatcher> mockedRegistrationManager;
    @InjectMocks
    private DispatcherConfigurationUpdateHandler testConfigHandler;

    private Map<DispatcherConfiguration, Set<String>> currentConfiguration;

    @Mock
    private EventCollector mockedEventCollector;

    @Test
    public void testLoadConfigurationWithoutReducedOutputPropertyAndWithoutEventContentType() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        String testDatastreamId1 = "test1";
        String testDatastreamId2 = "test2";
        String testDatastreamId3 = "test3";
        testProperties.put(testDatastreamId1, "30;30");
        testProperties.put(testDatastreamId2, "30");
        testProperties.put(testDatastreamId3, "10");

        testConfigHandler.loadConfiguration(testProperties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(2, currentConfiguration.size());
        assertEquals(new HashSet<>(Arrays.asList(testDatastreamId1, testDatastreamId2)),
                currentConfiguration.get(new DispatcherConfiguration(30,30)));
        assertEquals(Collections.singleton(testDatastreamId3),
                currentConfiguration.get(new DispatcherConfiguration(10,10)));
        assertFalse((boolean) Whitebox.getInternalState(testConfigHandler, "reducedOutput"));
        assertEquals(ContentType.JSON, Whitebox.getInternalState(testConfigHandler, "eventContentType"));
    }

    @Test
    public void testLoadConfigurationWithReducedOutputPropertyAsTrue() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(REDUCED_OUTPUT_PROPERTY_NAME, "true");

        testConfigHandler.loadConfiguration(testProperties);

        assertTrue((boolean) Whitebox.getInternalState(testConfigHandler, "reducedOutput"));
    }

    @Test
    public void testLoadConfigurationWithReducedOutputPropertyAsFalse() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(REDUCED_OUTPUT_PROPERTY_NAME, "false");

        testConfigHandler.loadConfiguration(testProperties);

        assertFalse((boolean) Whitebox.getInternalState(testConfigHandler, "reducedOutput"));
    }

    @Test
    public void testLoadConfigurationWithReducedOutputPropertyAsInvalidValue() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(REDUCED_OUTPUT_PROPERTY_NAME, "invalid");

        testConfigHandler.loadConfiguration(testProperties);

        assertFalse((boolean) Whitebox.getInternalState(testConfigHandler, "reducedOutput"));
    }

    @Test
    public void testLoadConfigurationWithEventContentTypeAsCbor() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(EVENT_CONTENT_TYPE_PROPERTY_NAME, "cbor");

        testConfigHandler.loadConfiguration(testProperties);

        assertEquals(ContentType.CBOR, Whitebox.getInternalState(testConfigHandler, "eventContentType"));
    }

    @Test
    public void testLoadConfigurationWithInvalidEventContentType() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(EVENT_CONTENT_TYPE_PROPERTY_NAME, "invalid");

        testConfigHandler.loadConfiguration(testProperties);

        assertEquals(ContentType.JSON, Whitebox.getInternalState(testConfigHandler, "eventContentType"));
    }

    @Test
    public void testLoadConfigurationInvalidConfigurationIsIgnoredAndContinueParsing() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put("testProperty", "ignored");
        String testDatastreamId1 = "test1";
        testProperties.put(testDatastreamId1, "30");

        testConfigHandler.loadConfiguration(testProperties);

        currentConfiguration = getCurrentConfiguration();
        assertEquals(1, currentConfiguration.size());
        assertEquals(Collections.singleton(testDatastreamId1),
                currentConfiguration.get(new DispatcherConfiguration(30,30)));
    }

    @Test
    public void testLoadDefaultConfiguration() {
        currentConfiguration = getCurrentConfiguration();
        currentConfiguration.put(new DispatcherConfiguration(30, 30), Collections.singleton("test1"));
        currentConfiguration.put(new DispatcherConfiguration(10, 60), Collections.singleton("test2"));
        currentConfiguration.put(new DispatcherConfiguration(10, 10), Collections.singleton("test3"));

        testConfigHandler.loadDefaultConfiguration();

        assertTrue(currentConfiguration.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private Map<DispatcherConfiguration, Set<String>> getCurrentConfiguration() {
        return (Map<DispatcherConfiguration, Set<String>>) Whitebox.getInternalState(testConfigHandler, "currentConfiguration");
    }


    @Test
    public void testApplyConfiguration() {
        currentConfiguration = new HashMap<>();
        Set<String> set1 = new HashSet<>(Arrays.asList("test1", "test2"));
        Set<String> set2 = new HashSet<>(Collections.singletonList("test3"));
        Collection<String> allDatastreams = new HashSet<>(set1);
        allDatastreams.addAll(set2);
        currentConfiguration.put(new DispatcherConfiguration(30,30), set1);
        currentConfiguration.put(new DispatcherConfiguration(60,10), set2);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", currentConfiguration);
        Whitebox.setInternalState(testConfigHandler, "eventDispatcherRegistrationManager",
                mockedRegistrationManager);

        when(mockedFactory.createEventCollector(anyBoolean(), any(ContentType.class))).thenReturn(mockedEventCollector);

        testConfigHandler.applyConfiguration();

        verify(mockedScheduler).clear();
        verify(mockedRegistrationManager).unregister();

        verify(mockedFactory).createEventCollector(eq(false), eq(ContentType.JSON));
        verify(mockedEventCollector).loadDatastreamIdsToCollect(eq(allDatastreams));

        verify(mockedScheduler).schedule(runnableCaptor.capture(), eq(30L), eq(30L));
        runnableCaptor.getValue().run();
        verify(mockedEventCollector).publishCollectedEvents(eq(set1));
        verify(mockedScheduler).schedule(runnableCaptor.capture(), eq(60L), eq(10L));
        runnableCaptor.getValue().run();
        verify(mockedEventCollector).publishCollectedEvents(eq(set2));
        verify(mockedRegistrationManager).register(eq(mockedEventCollector));
    }
}
