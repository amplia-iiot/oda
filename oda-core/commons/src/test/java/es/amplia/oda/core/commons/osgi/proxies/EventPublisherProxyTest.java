package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.utils.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EventPublisherProxyTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String[] TEST_PATH = new String[] { "gateway1", "gateway2"};
    private static final String TEST_FEED = "testFeed";

    private static final long TEST_AT = 123456789L;
    private static final double TEST_VALUE = 99.99;
    private static final String TEST_VALUE_2 = "testing";

    @Mock
    private BundleContext mockedContext;

    private EventPublisherProxy testEventPublisher;

    private MockedConstruction<StateManagerProxy> stateManagerConstruction;
    private final List<List<?>> stateManagerArgs = new ArrayList<>();
    private StateManagerProxy mockedStateManager;

    @Captor
    private ArgumentCaptor<List<Event>> eventCaptor;

    @Before
    public void setUp() throws Exception {
        stateManagerConstruction = mockConstruction(StateManagerProxy.class,
                (mock, mctx) -> stateManagerArgs.add(new ArrayList<>(mctx.arguments())));
        testEventPublisher = new EventPublisherProxy(mockedContext);
        mockedStateManager = stateManagerConstruction.constructed().get(0);
    }

    @After
    public void tearDown() {
        stateManagerConstruction.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(1, stateManagerConstruction.constructed().size());
        assertEquals(mockedContext, stateManagerArgs.get(0).get(0));
    }

    @Test
    public void testPublishGroupEvents() {
        Map<String, Map<String, Map<Long,Object>>> events = new HashMap<>();
        Map<String, Map<Long, Object>> eventsByFeed1 = new HashMap<>();
        Map<String, Map<Long, Object>> eventsByFeed2 = new HashMap<>();
        Map<Long, Object> event1 = new HashMap<>();
        event1.put(TEST_AT, TEST_VALUE);
        Map<Long, Object> event2 = new HashMap<>();
        event2.put(null, TEST_VALUE_2);
        eventsByFeed1.put(TEST_FEED, event1);
        eventsByFeed2.put(null, event2);
        events.put(TEST_DATASTREAM_ID, eventsByFeed1);
        events.put(TEST_DATASTREAM_ID_2, eventsByFeed2);

        // call method
        testEventPublisher.publishEvents(TEST_DEVICE_ID, TEST_PATH, events);

        // assertions
        verify(mockedStateManager).onReceivedEvents(eventCaptor.capture());
        List<Event> capturedEvents = eventCaptor.getValue();
        assertEquals(2, capturedEvents.size());
        assertEquals(TEST_DEVICE_ID, capturedEvents.get(0).getDeviceId());
        assertEquals(TEST_PATH,  capturedEvents.get(0).getPath());
        assertEquals(TEST_FEED,  capturedEvents.get(0).getFeed());
        assertEquals(TEST_DATASTREAM_ID,  capturedEvents.get(0).getDatastreamId());
        assertEquals(Optional.ofNullable(TEST_AT), Optional.ofNullable(capturedEvents.get(0).getAt()));
        assertEquals(TEST_VALUE,  capturedEvents.get(0).getValue());
        assertEquals(TEST_DEVICE_ID, capturedEvents.get(1).getDeviceId());
        assertEquals(TEST_PATH, capturedEvents.get(1).getPath());
        assertEquals(null,  capturedEvents.get(1).getFeed());
        assertEquals(TEST_DATASTREAM_ID_2, capturedEvents.get(1).getDatastreamId());
        assertNotNull(capturedEvents.get(1).getAt());
        assertEquals(TEST_VALUE_2, capturedEvents.get(1).getValue());
    }

    @Test
    public void testClose() {
        testEventPublisher.close();

        verify(mockedStateManager).close();
    }
}
