package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.core.commons.utils.OdaCommonConstants;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventCollectorImplTest {

    private static final String TEST_COLLECTED_DATASTREAM_ID = "collectedDatastream";
    private static final String TEST_NOT_COLLECTED_DATASTREAM_ID = "notCollectedDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] { "testGateway" };
    private static final String TEST_FEED = "testFeed";
    private static final long TEST_AT = System.currentTimeMillis();
    private static final long TEST_AT_2 = System.currentTimeMillis() + 500;
    private static final Object TEST_VALUE = 99;
    private static final String TEST_COLLECTED_DATASTREAM_ID_2 = "collectedDatastream2";
    private static final Object TEST_VALUE_2 = "meh";
    private static final Event TEST_NOT_COLLECTED_EVENT =
            new Event(TEST_NOT_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE);
    private static final Event TEST_COLLECTED_EVENT =
            new Event(TEST_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_FEED, TEST_AT, TEST_VALUE);
    private static final Event TEST_COLLECTED_EVENT_2 =
            new Event(TEST_COLLECTED_DATASTREAM_ID_2, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE_2);
    private static final Event TEST_COLLECTED_EVENT_3 =
            new Event(TEST_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE);
    private static final Event TEST_COLLECTED_EVENT_4 =
            new Event(TEST_COLLECTED_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT_2, TEST_VALUE_2);


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
        testEventCollector.publish(Collections.singletonList(TEST_NOT_COLLECTED_EVENT));

        verify(mockedEventDispatcher).publish(eq(Collections.singletonList(TEST_NOT_COLLECTED_EVENT)));
        verifyZeroInteractions(spiedCollectedEvents);
    }

    @Test
    public void testPublishCollectedMultipleDatastream() {
        when(spiedDatastreamIdsToCollect.contains(anyString())).thenReturn(true);
        List<Event> events = new ArrayList<>();
        events.add(TEST_COLLECTED_EVENT);
        events.add(TEST_COLLECTED_EVENT_2);

        testEventCollector.publish(events);

        verify(mockedEventDispatcher, never()).parse(events);
        verify(spiedCollectedEvents).merge(eq(TEST_COLLECTED_DATASTREAM_ID_2), any(), any());
        assertEquals(Collections.singletonList(TEST_COLLECTED_EVENT),
                spiedCollectedEvents.get(TEST_COLLECTED_DATASTREAM_ID));
        assertEquals(Collections.singletonList(TEST_COLLECTED_EVENT_2),
                spiedCollectedEvents.get(TEST_COLLECTED_DATASTREAM_ID_2));
    }

    @Test
    public void testPublishCollectWithJoin() {
        spiedDatastreamIdsToCollect.add(TEST_COLLECTED_DATASTREAM_ID);
        Event oldCollectedEvent =
                new Event(TEST_COLLECTED_DATASTREAM_ID, "otherDevice", null, null, System.currentTimeMillis(), "Hello!");
        spiedCollectedEvents.put(TEST_COLLECTED_DATASTREAM_ID, Collections.singletonList(oldCollectedEvent));

        testEventCollector.publish(Collections.singletonList(TEST_COLLECTED_EVENT));

        verify(spiedCollectedEvents).merge(eq(TEST_COLLECTED_DATASTREAM_ID), any(), any());
        assertEquals(Arrays.asList(oldCollectedEvent, TEST_COLLECTED_EVENT),
                spiedCollectedEvents.get(TEST_COLLECTED_DATASTREAM_ID));
    }



    @Test
    public void testEventsSameDatastreamIdDiferentFeed() {
        // set events 1 to 4 as collected events
        spiedCollectedEvents.clear();
        spiedCollectedEvents.put(TEST_COLLECTED_DATASTREAM_ID, Arrays.asList(TEST_COLLECTED_EVENT, TEST_COLLECTED_EVENT_3, TEST_COLLECTED_EVENT_4));
        spiedCollectedEvents.put(TEST_COLLECTED_DATASTREAM_ID_2, Collections.singletonList(TEST_COLLECTED_EVENT_2));

        // prepare expected response
        List<Datastream> datastreamList = Arrays.asList(
                new Datastream(TEST_COLLECTED_DATASTREAM_ID, TEST_FEED, Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE))),
                new Datastream(TEST_COLLECTED_DATASTREAM_ID, null,
                        new HashSet<>(Arrays.asList(new Datapoint(TEST_AT, TEST_VALUE), new Datapoint(TEST_AT_2, TEST_VALUE_2)))),
                new Datastream(TEST_COLLECTED_DATASTREAM_ID_2, null, Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE_2)))
                );

        OutputDatastream odReturn = new OutputDatastream(OdaCommonConstants.OPENGATE_VERSION,
                TEST_DEVICE_ID, TEST_PATH, new HashSet<>(datastreamList));


        // conditions
        OutputDatastream od1 = new OutputDatastream(OdaCommonConstants.OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH,
                Collections.singleton(new Datastream(TEST_COLLECTED_DATASTREAM_ID, TEST_FEED,
                        Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE)))));

        OutputDatastream od2 = new OutputDatastream(OdaCommonConstants.OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH,
                Collections.singleton(new Datastream(TEST_COLLECTED_DATASTREAM_ID_2, null,
                        Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE_2)))));

        OutputDatastream od3 = new OutputDatastream(OdaCommonConstants.OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH,
                Collections.singleton(new Datastream(TEST_COLLECTED_DATASTREAM_ID, null,
                        Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE)))));

        OutputDatastream od4 = new OutputDatastream(OdaCommonConstants.OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH,
                Collections.singleton(new Datastream(TEST_COLLECTED_DATASTREAM_ID, null,
                        Collections.singleton(new Datapoint(TEST_AT_2, TEST_VALUE_2)))));

        when(mockedEventDispatcher.parse(Collections.singletonList(any(Event.class))))
                .thenReturn(od1).thenReturn(od2).thenReturn(od3).thenReturn(od4);

        // call to publish collected events
        testEventCollector.publishCollectedEvents(Arrays.asList(TEST_COLLECTED_DATASTREAM_ID, TEST_COLLECTED_DATASTREAM_ID_2));

        verify(mockedEventDispatcher).parse(Collections.singletonList(TEST_COLLECTED_EVENT));
        verify(mockedEventDispatcher).parse(Collections.singletonList(TEST_COLLECTED_EVENT_2));
        verify(mockedEventDispatcher).parse(Collections.singletonList(TEST_COLLECTED_EVENT_3));
        verify(mockedEventDispatcher).parse(Collections.singletonList(TEST_COLLECTED_EVENT_4));

        verify(mockedEventDispatcher).send(odReturn);

        verify(mockedEventDispatcher).send(outputDatastreamCaptor.capture());
        OutputDatastream outputDatastream = outputDatastreamCaptor.getValue();
        List<Datastream> sentDatastreams = new ArrayList<>(outputDatastream.getDatastreams());
        assertEquals(3, sentDatastreams.size());

        sentDatastreams.forEach(datastream -> {
            if (datastream.getId().equals(TEST_COLLECTED_DATASTREAM_ID)) {

                // for datastreamId TEST_COLLECTED_DATASTREAM_ID and feed null, there must be 2 datapoints
                if (datastream.getFeed() == null) {
                    assertEquals(2, datastream.getDatapoints().size());
                }
                // for datastreamId TEST_COLLECTED_DATASTREAM_ID and feed TEST_FEED, there must be 1 datapoints
                else if (datastream.getFeed().equals(TEST_FEED)) {
                    assertEquals(1, datastream.getDatapoints().size());
                }
            }
            // for datastreamId TEST_COLLECTED_DATASTREAM_ID_2 there must be 1 datapoints
            else if (datastream.getId().equals(TEST_COLLECTED_DATASTREAM_ID_2)) {
                assertEquals(1, datastream.getDatapoints().size());
            }
        });
    }
}