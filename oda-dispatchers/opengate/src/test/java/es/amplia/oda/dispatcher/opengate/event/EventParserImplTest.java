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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventParserImplTest {

    private static final long TEST_AT = System.currentTimeMillis();
    private static final long TEST_AT_2 = System.currentTimeMillis() + 500;
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_FEED = "testFeed";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final String TEST_HOST_ID = "testHost";
    private static final String[] TEST_PATH = new String[] { "path", "to", "device"};
    private static final String TEST_VALUE = "test";
    private static final Event TEST_EVENT =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE);
    private static final Event TEST_EVENT_NULL_PATH =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, TEST_AT, TEST_VALUE);
    private static final int TEST_VALUE_2 = 782;
    private static final Event TEST_EVENT_2 =
            new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, TEST_PATH, null, null, TEST_VALUE_2);




    private static final Event TEST_COLLECTED_EVENT =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_FEED, TEST_AT, TEST_VALUE);
    private static final Event TEST_COLLECTED_EVENT_2 =
            new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE_2);
    private static final Event TEST_COLLECTED_EVENT_3 =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT, TEST_VALUE);
    private static final Event TEST_COLLECTED_EVENT_4 =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, null, TEST_AT_2, TEST_VALUE_2);
    private static final Event TEST_COLLECTED_EVENT_5 =
            new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID_2, TEST_PATH, null, TEST_AT_2, TEST_VALUE_2);



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

        List<OutputDatastream> outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.get(0).getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.get(0).getDevice());
        assertArrayEquals(TEST_PATH, outputDatastream.get(0).getPath());
        List<Datastream> datastreams = outputDatastream.get(0).getDatastreams();
        assertNotNull(datastreams);
        assertFalse(datastreams.isEmpty());
        assertEquals(2, datastreams.size());
    }

    @Test
    public void testParseNotNullPath() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT_NULL_PATH);

        List<OutputDatastream> outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.get(0).getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.get(0).getDevice());
        assertNull(outputDatastream.get(0).getPath());
        List<Datastream> datastreams = outputDatastream.get(0).getDatastreams();
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

        List<OutputDatastream> outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.get(0).getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.get(0).getDevice());
        assertArrayEquals(TEST_PATH, outputDatastream.get(0).getPath());
        List<Datastream> datastreams = outputDatastream.get(0).getDatastreams();
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

        List<OutputDatastream> outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.get(0).getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.get(0).getDevice());
        assertArrayEquals(new String[] { TEST_HOST_ID }, outputDatastream.get(0).getPath());
        List<Datastream> datastreams = outputDatastream.get(0).getDatastreams();
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

        List<OutputDatastream> outputDatastream = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, outputDatastream.get(0).getVersion());
        assertEquals(TEST_DEVICE_ID, outputDatastream.get(0).getDevice());
        assertArrayEquals(expectedPath, outputDatastream.get(0).getPath());
        List<Datastream> datastreams = outputDatastream.get(0).getDatastreams();
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
    public void testEventsSameDatastreamIdDiferentFeed() {

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_COLLECTED_EVENT);
        events.add(TEST_COLLECTED_EVENT_2);
        events.add(TEST_COLLECTED_EVENT_3);
        events.add(TEST_COLLECTED_EVENT_4);

        // prepare expected response
        List<Datastream> datastreamList = Arrays.asList(
                new Datastream(TEST_DATASTREAM_ID, TEST_FEED, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE))),
                new Datastream(TEST_DATASTREAM_ID, null,
                        Arrays.asList(new Datapoint(TEST_AT, TEST_VALUE), new Datapoint(TEST_AT_2, TEST_VALUE_2))),
                new Datastream(TEST_DATASTREAM_ID_2, null, Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE_2)))
        );

        List<OutputDatastream> expectedOutput = Collections.singletonList(new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH, datastreamList));

        // call method to test
        List<OutputDatastream> returnedOutput = testEventParser.parse(events);

        // check result
        assertEquals(expectedOutput, returnedOutput);
        assertEquals(1, returnedOutput.size());
        List<Datastream> returnedDatastreams = new ArrayList<>(returnedOutput.get(0).getDatastreams());
        assertEquals(3, returnedDatastreams.size());

        returnedDatastreams.forEach(datastream -> {
            if (datastream.getId().equals(TEST_DATASTREAM_ID)) {

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
            else if (datastream.getId().equals(TEST_DATASTREAM_ID_2)) {
                assertEquals(1, datastream.getDatapoints().size());
            }
        });
    }

    @Test
    public void testEventsSameDatastreamIdDiferentDeviceId() {

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);

        List<Event> events = new ArrayList<>();
        events.add(TEST_COLLECTED_EVENT);
        events.add(TEST_COLLECTED_EVENT_5);

        // prepare expected response
        List<OutputDatastream> expectedOutput = Arrays.asList(
                new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH, Collections.singletonList(
                        new Datastream(TEST_DATASTREAM_ID, TEST_FEED,
                                Collections.singletonList(new Datapoint(TEST_AT, TEST_VALUE)))))
                , new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID_2, TEST_PATH, Collections.singletonList(
                        new Datastream(TEST_DATASTREAM_ID, null, Collections.singletonList(
                                new Datapoint(TEST_AT_2, TEST_VALUE_2))))));

        // call method to test
        List<OutputDatastream> returnedOutput = testEventParser.parse(events);

        // check result
        assertEquals(expectedOutput, returnedOutput);
        assertEquals(2, returnedOutput.size());
        List<Datastream> returnedDatastreams = new ArrayList<>(returnedOutput.get(0).getDatastreams());
        assertEquals(1, returnedDatastreams.size());
        List<Datastream> returnedDatastreams2 = new ArrayList<>(returnedOutput.get(1).getDatastreams());
        assertEquals(1, returnedDatastreams2.size());
    }
}