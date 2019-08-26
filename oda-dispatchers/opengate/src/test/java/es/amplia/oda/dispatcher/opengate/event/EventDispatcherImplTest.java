package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventDispatcherImplTest {

    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final int TEST_VALUE = 9;
    private static final Event TEST_EVENT =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, System.currentTimeMillis(), TEST_VALUE);
    private static final OutputDatastream TEST_OUTPUT_DATASTREAM =
            new OutputDatastream("8.0", TEST_DEVICE_ID, null, Collections.singleton(
                    new Datastream(TEST_DATASTREAM_ID, Collections.singleton(new Datapoint(TEST_AT, TEST_VALUE)))));
    private static final byte[] TEST_BYTE_STREAM = new byte[] {1,2,3,4};

    @Mock
    private EventParser mockedEventParser;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private OpenGateConnector mockedConnector;
    @InjectMocks
    private EventDispatcherImpl testEventDispatcher;


    @Test
    public void testPublish() throws IOException {
        when(mockedEventParser.parse(any(Event.class))).thenReturn(TEST_OUTPUT_DATASTREAM);
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM);

        testEventDispatcher.publish(TEST_EVENT);

        verify(mockedEventParser).parse(eq(TEST_EVENT));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
        verify(mockedConnector).uplink(eq(TEST_BYTE_STREAM));
    }

    @Test
    public void testParse() {
        when(mockedEventParser.parse(any(Event.class))).thenReturn(TEST_OUTPUT_DATASTREAM);

        OutputDatastream result = testEventDispatcher.parse(TEST_EVENT);

        assertEquals(TEST_OUTPUT_DATASTREAM, result);
        verify(mockedEventParser).parse(eq(TEST_EVENT));
    }

    @Test
    public void testPublishOutputDatastream() throws IOException {
        when(mockedSerializer.serialize(any())).thenReturn(TEST_BYTE_STREAM);

        testEventDispatcher.publish(TEST_OUTPUT_DATASTREAM);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
        verify(mockedConnector).uplink(eq(TEST_BYTE_STREAM));
    }

    @Test
    public void testPublishOutputDatastreamIOExceptionCaught() throws IOException {
        when(mockedSerializer.serialize(any())).thenThrow(new IOException());

        testEventDispatcher.publish(TEST_OUTPUT_DATASTREAM);

        verify(mockedSerializer).serialize(eq(TEST_OUTPUT_DATASTREAM));
    }
}