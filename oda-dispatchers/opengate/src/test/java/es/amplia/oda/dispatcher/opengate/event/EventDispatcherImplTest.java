package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventDispatcherImplTest {

    private static final ContentType TEST_CONTENT_TYPE = ContentType.MESSAGE_PACK;
    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_FEED = "testFeed";
    private static final int TEST_VALUE = 9;
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final int TEST_VALUE_2 = 53;
    private static final Event TEST_EVENT =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, System.currentTimeMillis(), TEST_VALUE);
    private static final Event TEST_EVENT_2 =
            new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, null, null, System.currentTimeMillis(), TEST_VALUE_2);

    private static final OutputDatastream TEST_OUTPUT_DATASTREAM =
            new OutputDatastream("8.0", TEST_DEVICE_ID, null, Collections.singletonList(
                    new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE)))));
    private static final OutputDatastream TEST_OUTPUT_DATASTREAM_VALUE_2 =
            new OutputDatastream("8.0", TEST_DEVICE_ID, null, Collections.singletonList(
                    new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE_2)))));
    private static final OutputDatastream TEST_OUTPUT_DATASTREAM_2 =
            new OutputDatastream("8.0", TEST_DEVICE_ID, null, Collections.singletonList(
                    new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Arrays.asList(new Datapoint(TEST_AT, TEST_VALUE), new Datapoint(TEST_AT, TEST_VALUE_2)))));
    private static final OutputDatastream TEST_OUTPUT_DATASTREAM_3 =
            new OutputDatastream("8.0", TEST_DEVICE_ID, null, Arrays.asList(
                    new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Arrays.asList(new Datapoint(TEST_AT, TEST_VALUE), new Datapoint(TEST_AT, TEST_VALUE_2))),
                    new Datastream(TEST_DATASTREAM_ID_2, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE)))));
    private static final OutputDatastream TEST_OUTPUT_DATASTREAM_3_VALUE_2 =
            new OutputDatastream("8.0", TEST_DEVICE_ID, null, Arrays.asList(
                    new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE))),
                    new Datastream(TEST_DATASTREAM_ID_2, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE)))));

    private static final byte[] TEST_BYTE_STREAM = new byte[] {1,2,3,4};
    private static final byte[] TEST_BYTE_STREAM8 = new byte[] {1,2,3,4,5,6,7,8};

    @Mock
    private EventParser mockedEventParser;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private OpenGateConnector mockedConnector;
    @Mock
    private Scheduler scheduler;
    private EventDispatcherImpl testEventDispatcher;


    @Before
    public void setUp() {
        testEventDispatcher =
                new EventDispatcherImpl(mockedEventParser, mockedSerializer, TEST_CONTENT_TYPE, mockedConnector, scheduler);
    }

    @Test
    public void testPublishGroup() throws IOException {
        List<Datastream> datastreams = new ArrayList<>();
        datastreams.add(new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE_2))));
        datastreams.add(new Datastream(TEST_DATASTREAM_ID_2, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE_2))));
        OutputDatastream outputDatastream =
                new OutputDatastream("8.0", TEST_DEVICE_ID, null, datastreams);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);
        events.add(TEST_EVENT_2);

        when(mockedEventParser.parse(any(List.class))).thenReturn(outputDatastream);
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM);

        testEventDispatcher.publish(events);

        verify(mockedEventParser).parse(eq(events));
        //verify(testEventDispatcher).send(eq(outputDatastream));
    }

    @Test
    public void testPublishOutputDatastream() throws IOException {
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM);

        testEventDispatcher.send(TEST_OUTPUT_DATASTREAM);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
        verify(mockedConnector).uplink(eq(TEST_BYTE_STREAM), eq(TEST_CONTENT_TYPE));
    }

    @Test
    public void testPublishOutputDatastreamIOExceptionCaught() throws IOException {
        when(mockedSerializer.serialize(any())).thenThrow(new IOException());

        testEventDispatcher.send(TEST_OUTPUT_DATASTREAM);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
    }

    @Test
    public void testPublishOutputDatastreamMaxlength() throws IOException {
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM8, TEST_BYTE_STREAM, TEST_BYTE_STREAM);
        when(mockedConnector.hasMaxlength()).thenReturn(true);
        when(mockedConnector.getMaxLength()).thenReturn(6);

        testEventDispatcher.send(TEST_OUTPUT_DATASTREAM_2);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM_2));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM_VALUE_2));
        verify(mockedConnector, times(2)).uplink(eq(TEST_BYTE_STREAM), eq(TEST_CONTENT_TYPE));
    }

    @Test
    public void testPublishOutputDatastreamMaxlengthComplex() throws IOException {
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM8, TEST_BYTE_STREAM, TEST_BYTE_STREAM);
        when(mockedConnector.hasMaxlength()).thenReturn(true);
        when(mockedConnector.getMaxLength()).thenReturn(6);

        testEventDispatcher.send(TEST_OUTPUT_DATASTREAM_3);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM_3));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM_3_VALUE_2));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM_VALUE_2));
        verify(mockedConnector, times(2)).uplink(eq(TEST_BYTE_STREAM), eq(TEST_CONTENT_TYPE));
    }

    @Test
    public void testPublishOutputDatastreamMaxlengthReached() throws IOException {
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM);
        when(mockedConnector.hasMaxlength()).thenReturn(true);
        when(mockedConnector.getMaxLength()).thenReturn(1);

        testEventDispatcher.send(TEST_OUTPUT_DATASTREAM);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
    }

}