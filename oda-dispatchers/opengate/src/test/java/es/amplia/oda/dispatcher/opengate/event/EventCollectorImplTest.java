package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventCollectorImplTest {

    private static final String TEST_COLLECTED_DATASTREAM_ID = "collectedDatastream";
    private static final String TEST_NOT_COLLECTED_DATASTREAM_ID = "notCollectedDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] { "testGateway" };
    private static final long TEST_AT = System.currentTimeMillis();
    private static final Object TEST_VALUE = 99;
    private static final Event TEST_NOT_COLLECTED_EVENT =
            new Event(TEST_NOT_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);
    private static final Event TEST_COLLECTED_EVENT =
            new Event(TEST_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);


    @Mock
    private EventDispatcherImpl mockedEventDispatcher;
    @Spy
    private ArrayList<String> spiedDatastreamIdsToCollect;
    @Spy
    private HashMap<String, List<Event>> spiedCollectedEvents;
    private EventCollectorImpl testEventCollector;

    @Captor
    ArgumentCaptor<OutputDatastream> outputDatastreamCaptor;


    @Before
    public void setUp() {
        testEventCollector = new EventCollectorImpl(mockedEventDispatcher);

        Whitebox.setInternalState(testEventCollector, "datastreamIdsToCollect", spiedDatastreamIdsToCollect);
        Whitebox.setInternalState(testEventCollector, "collectedEvents", spiedCollectedEvents);
    }

    @Test
    public void testLoadDatastreamIdsToCollect() {
        Collection<String> datastreamIds = Arrays.asList("testDatastream1", "testDatastream2", "testDatastream3");

        testEventCollector.loadDatastreamIdsToCollect(datastreamIds);

        verify(spiedDatastreamIdsToCollect).clear();
        verify(spiedDatastreamIdsToCollect).addAll(eq(datastreamIds));
    }

    @Test
    public void testPublishNotCollectedDatastream() {
        testEventCollector.publish(TEST_NOT_COLLECTED_EVENT);

        verify(mockedEventDispatcher).publish(eq(TEST_NOT_COLLECTED_EVENT));
        verifyZeroInteractions(spiedCollectedEvents);
    }

    @Test
    public void testPublishCollectedDatastream() {
        when(spiedDatastreamIdsToCollect.contains(anyString())).thenReturn(true);

        testEventCollector.publish(TEST_COLLECTED_EVENT);

        verifyZeroInteractions(mockedEventDispatcher);
        verify(spiedCollectedEvents).merge(eq(TEST_COLLECTED_DATASTREAM_ID), any(), any());
        assertEquals(Collections.singletonList(TEST_COLLECTED_EVENT),
                spiedCollectedEvents.get(TEST_COLLECTED_DATASTREAM_ID));
    }

    @Test
    public void testPublishCollectWithJoin() {
        spiedDatastreamIdsToCollect.add(TEST_COLLECTED_DATASTREAM_ID);
        Event oldCollectedEvent =
                new Event(TEST_COLLECTED_DATASTREAM_ID, "otherDevice", null, System.currentTimeMillis(), "Hello!");
        spiedCollectedEvents.put(TEST_COLLECTED_DATASTREAM_ID, Collections.singletonList(oldCollectedEvent));

        testEventCollector.publish(TEST_COLLECTED_EVENT);

        verify(spiedCollectedEvents).merge(eq(TEST_COLLECTED_DATASTREAM_ID), any(), any());
        assertEquals(Arrays.asList(oldCollectedEvent, TEST_COLLECTED_EVENT),
                spiedCollectedEvents.get(TEST_COLLECTED_DATASTREAM_ID));
    }

    @Test
    public void testPublishCollectedEvents() {
        String otherDatastreamId = "otherDatastream";
        String otherDeviceId = "otherDevice";
        List<String> collectedDatastreams = Arrays.asList(TEST_COLLECTED_DATASTREAM_ID, otherDatastreamId);
        long testAt1 = 0L;
        Object testValue1 = "testValue1";
        Event testEvent1 = new Event(otherDatastreamId, otherDeviceId, null, testAt1, testValue1);
        Object testValue2 = "testValue2";
        Event testEvent2 = new Event(otherDatastreamId, TEST_DEVICE_ID, TEST_PATH, TEST_AT, testValue2);
        long testAt3 = System.currentTimeMillis();
        Object testValue3 = 101;
        Event testEvent3 = new Event(TEST_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, testAt3, testValue3);
        spiedCollectedEvents.put(TEST_COLLECTED_DATASTREAM_ID, Arrays.asList(TEST_COLLECTED_EVENT, testEvent3));
        spiedCollectedEvents.put(otherDatastreamId, Arrays.asList(testEvent1, testEvent2));
        OutputDatastream od1 = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH, Collections.singleton(
                new Datastream(TEST_COLLECTED_DATASTREAM_ID, Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE)))));
        OutputDatastream od2 = new OutputDatastream(OPENGATE_VERSION, otherDeviceId, null, Collections.singleton(
                new Datastream(otherDatastreamId, Collections.singleton(new Datapoint(testAt1, testValue1)))));
        OutputDatastream od3 = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH, Collections.singleton(
                new Datastream(otherDatastreamId, Collections.singleton(new Datapoint(TEST_AT, testValue2)))));
        OutputDatastream od4 = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH, Collections.singleton(
                new Datastream(TEST_COLLECTED_DATASTREAM_ID, Collections.singleton(new Datapoint(testAt3, testValue3)))));

        when(mockedEventDispatcher.parse(any(Event.class))).thenReturn(od1).thenReturn(od4).thenReturn(od2).thenReturn(od3);

        testEventCollector.publishCollectedEvents(collectedDatastreams);

        verify(mockedEventDispatcher).parse(TEST_COLLECTED_EVENT);
        verify(mockedEventDispatcher).parse(testEvent1);
        verify(mockedEventDispatcher).parse(testEvent2);
        verify(mockedEventDispatcher).parse(testEvent3);
        verify(mockedEventDispatcher, times(2)).publish(outputDatastreamCaptor.capture());
        List<OutputDatastream> outputDatastreams = outputDatastreamCaptor.getAllValues();
        assertEquals(2, outputDatastreams.size());
        Optional<OutputDatastream> outputDatastreamWithDeviceId =
                outputDatastreams.stream().filter(od -> TEST_DEVICE_ID.equals(od.getDevice())).findFirst();
        assertTrue(outputDatastreamWithDeviceId.isPresent());
        OutputDatastream outputDatastream = outputDatastreamWithDeviceId.get();
        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertArrayEquals(TEST_PATH, outputDatastream.getPath());
        Set<Datastream> datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertEquals(2, datastreams.size());
        Optional<Datastream> datastreamWithId = datastreams.stream()
                .filter(d -> TEST_COLLECTED_DATASTREAM_ID.equals(d.getId())).findFirst();
        assertTrue(datastreamWithId.isPresent());
        Datastream datastream = datastreamWithId.get();
        Set<Datapoint> datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertEquals(2, datapoints.size());
        verifyDatapoint(TEST_AT, TEST_VALUE, datapoints);
        verifyDatapoint(testAt3, testValue3, datapoints);
        Optional<Datastream> datastreamWithOtherDatastreamId = datastreams.stream()
                .filter(d -> otherDatastreamId.equals(d.getId())).findFirst();
        assertTrue(datastreamWithOtherDatastreamId.isPresent());
        datastream = datastreamWithOtherDatastreamId.get();
        datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertEquals(1, datapoints.size());
        verifyDatapoint(TEST_AT, testValue2, datapoints);
        outputDatastreamWithDeviceId = outputDatastreams.stream()
                .filter(od -> otherDeviceId.equals(od.getDevice())).findFirst();
        assertTrue(outputDatastreamWithDeviceId.isPresent());
        outputDatastream = outputDatastreamWithDeviceId.get();
        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertNull(outputDatastream.getPath());
        datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertEquals(1, datastreams.size());
        datastreamWithId = datastreams.stream().filter(d -> otherDatastreamId.equals(d.getId())).findFirst();
        assertTrue(datastreamWithId.isPresent());
        datastream = datastreamWithId.get();
        datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertEquals(1, datapoints.size());
        verifyDatapoint(testAt1, testValue1, datapoints);

    }

    private void verifyDatapoint(long at, Object value, Set<Datapoint> datapoints) {
        assertNotNull(datapoints);
        if (datapoints.stream().noneMatch(dp -> at == dp.getAt() && value.equals(dp.getValue()))) {
            fail("No datapoint found with at " + at + "and value " + value);
        }
    }
}