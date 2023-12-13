package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventParserImplTest {

    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_HOST_ID = "testHost";
    private static final String[] TEST_PATH = new String[] { "path", "to", "device"};
    private static final String TEST_VALUE = "test";
    private static final Event TEST_EVENT =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE);
    private static final Event TEST_EVENT_NULL_PATH =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, TEST_AT, TEST_VALUE);
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final int TEST_VALUE_2 = 782;
    private static final Event TEST_EVENT_2 =
            new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, TEST_PATH, null, null, TEST_VALUE_2);

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @InjectMocks
    private EventParserImpl testEventParser;

    @Test
    public void testParseGroup() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);
        events.add(TEST_EVENT_2);

        OutputDatastream outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.getDevice());
        assertArrayEquals(TEST_PATH, outputDatastream.getPath());
        List<Datastream> datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertFalse(datastreams.isEmpty());
        assertEquals(2, datastreams.size());
    }

    @Test
    public void testParseNotNullPath() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT_NULL_PATH);

        OutputDatastream outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.getDevice());
        assertNull(outputDatastream.getPath());
        List<Datastream> datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertFalse(datastreams.isEmpty());
        assertEquals(1, datastreams.size());
        Datastream datastream = datastreams.toArray(new Datastream[0])[0];
        assertNotNull(datastream);
        assertEquals(TEST_DATASTREAM_ID, datastream.getId());
        List<Datapoint> datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertFalse(datapoints.isEmpty());
        assertEquals(1, datapoints.size());
        Datapoint datapoint = datapoints.toArray(new Datapoint[0])[0];
        assertNotNull(datapoint);
        assertEquals(TEST_AT, (long) datapoint.getAt());
        assertEquals(TEST_VALUE, datapoint.getValue());
        verify(mockedDeviceInfoProvider).getDeviceId();
    }

    @Test
    public void testParseNullHostId() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);

        OutputDatastream outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.getDevice());
        assertArrayEquals(TEST_PATH, outputDatastream.getPath());
        List<Datastream> datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertFalse(datastreams.isEmpty());
        assertEquals(1, datastreams.size());
        Datastream datastream = datastreams.toArray(new Datastream[0])[0];
        assertNotNull(datastream);
        assertEquals(TEST_DATASTREAM_ID, datastream.getId());
        List<Datapoint> datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertFalse(datapoints.isEmpty());
        assertEquals(1, datapoints.size());
        Datapoint datapoint = datapoints.toArray(new Datapoint[0])[0];
        assertNotNull(datapoint);
        assertEquals(TEST_AT, (long) datapoint.getAt());
        assertEquals(TEST_VALUE, datapoint.getValue());
        verify(mockedDeviceInfoProvider).getDeviceId();
    }

    @Test
    public void testParseDifferentHostIdAndDeviceIdNullPath() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT_NULL_PATH);

        OutputDatastream outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.getDevice());
        assertArrayEquals(new String[] { TEST_HOST_ID }, outputDatastream.getPath());
        List<Datastream> datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertFalse(datastreams.isEmpty());
        assertEquals(1, datastreams.size());
        Datastream datastream = datastreams.toArray(new Datastream[0])[0];
        assertNotNull(datastream);
        assertEquals(TEST_DATASTREAM_ID, datastream.getId());
        List<Datapoint> datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertFalse(datapoints.isEmpty());
        assertEquals(1, datapoints.size());
        Datapoint datapoint = datapoints.toArray(new Datapoint[0])[0];
        assertNotNull(datapoint);
        assertEquals(TEST_AT, (long) datapoint.getAt());
        assertEquals(TEST_VALUE, datapoint.getValue());
        verify(mockedDeviceInfoProvider).getDeviceId();
    }

    @Test
    public void testParseDifferentHostIdAndDeviceIdWithPath() {
        String[] expectedPath = new String[TEST_PATH.length + 1];
        System.arraycopy(TEST_PATH, 0, expectedPath, 1, TEST_PATH.length);
        expectedPath[0] = TEST_HOST_ID;

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);

        OutputDatastream outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.getDevice());
        assertArrayEquals(expectedPath, outputDatastream.getPath());
        List<Datastream> datastreams = outputDatastream.getDatastreams();
        assertNotNull(datastreams);
        assertFalse(datastreams.isEmpty());
        assertEquals(1, datastreams.size());
        Datastream datastream = datastreams.toArray(new Datastream[0])[0];
        assertNotNull(datastream);
        assertEquals(TEST_DATASTREAM_ID, datastream.getId());
        List<Datapoint> datapoints = datastream.getDatapoints();
        assertNotNull(datapoints);
        assertFalse(datapoints.isEmpty());
        assertEquals(1, datapoints.size());
        Datapoint datapoint = datapoints.toArray(new Datapoint[0])[0];
        assertNotNull(datapoint);
        assertEquals(TEST_AT, (long) datapoint.getAt());
        assertEquals(TEST_VALUE, datapoint.getValue());
        verify(mockedDeviceInfoProvider).getDeviceId();
    }
}