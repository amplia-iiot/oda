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
import java.util.Optional;
import java.util.Set;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventParserReducedOutputImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_HOST_ID = "testHost";
    private static final String[] TEST_PATH = new String[] { "testGateway" };
    private static final long TEST_AT = System.currentTimeMillis();
    private static final Object TEST_VALUE = 111;
    private static final Event TEST_EVENT = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final Object TEST_VALUE_2 = 42;
    private static final Event TEST_EVENT_2 = new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, TEST_PATH, null, TEST_VALUE_2);


    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @InjectMocks
    private EventParserReducedOutputImpl testEventParser;


    @Test
    public void testParse() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);

        OutputDatastream output = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        assertEquals(TEST_DEVICE_ID, output.getDevice());
        assertNull(output.getPath());

        Set<Datastream> datastreams = output.getDatastreams();
        assertNotNull(datastreams);
        assertEquals(1, datastreams.size());
        Optional<Datastream> datastreamWithId = datastreams.stream()
                .filter(d -> TEST_DATASTREAM_ID.equals(d.getId())).findFirst();
        if (!datastreamWithId.isPresent()) {
            fail("Datastream with id " + TEST_DATASTREAM_ID + " not found");
        }

        Set<Datapoint> datapoints = datastreamWithId.get().getDatapoints();
        assertNotNull(datapoints);
        assertEquals(1, datapoints.size());
        if (datapoints.stream().noneMatch(dp -> dp.getAt() == null && TEST_VALUE.equals(dp.getValue()))) {
            fail("Datapoint with null at and value " + TEST_VALUE + " not found");
        }
    }

    @Test
    public void testParseGroup() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);
        events.add(TEST_EVENT_2);

        OutputDatastream output = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        assertEquals(TEST_DEVICE_ID, output.getDevice());
        assertNull(output.getPath());

        Set<Datastream> datastreams = output.getDatastreams();
        assertNotNull(datastreams);
        assertEquals(2, datastreams.size());
        Optional<Datastream> datastreamWithId = datastreams.stream()
                .filter(d -> TEST_DATASTREAM_ID.equals(d.getId())).findFirst();
        if (!datastreamWithId.isPresent()) {
            fail("Datastream with id " + TEST_DATASTREAM_ID + " not found");
        }

        Set<Datapoint> datapoints = datastreamWithId.get().getDatapoints();
        assertNotNull(datapoints);
        assertEquals(1, datapoints.size());
        if (datapoints.stream().noneMatch(dp -> dp.getAt() == null && TEST_VALUE.equals(dp.getValue()))) {
            fail("Datapoint with null at and value " + TEST_VALUE + " not found");
        }
        datastreamWithId = datastreams.stream()
                .filter(d -> TEST_DATASTREAM_ID_2.equals(d.getId())).findFirst();
        if (!datastreamWithId.isPresent()) {
            fail("Datastream with id " + TEST_DATASTREAM_ID_2 + " not found");
        }

        datapoints = datastreamWithId.get().getDatapoints();
        assertNotNull(datapoints);
        assertEquals(1, datapoints.size());
        if (datapoints.stream().noneMatch(dp -> dp.getAt() == null && TEST_VALUE_2.equals(dp.getValue()))) {
            fail("Datapoint with null at and value " + TEST_VALUE_2 + " not found");
        }
    }

    @Test
    public void testParseSameDeviceAndHostId() {
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        List<Event> events = new ArrayList<>();
        events.add(TEST_EVENT);

        OutputDatastream output = testEventParser.parse(events);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        assertNull(output.getDevice());
        assertNull(output.getPath());

        Set<Datastream> datastreams = output.getDatastreams();
        assertNotNull(datastreams);
        assertEquals(1, datastreams.size());
        Optional<Datastream> datastreamWithId = datastreams.stream()
                .filter(d -> TEST_DATASTREAM_ID.equals(d.getId())).findFirst();
        if (!datastreamWithId.isPresent()) {
            fail("Datastream with id " + TEST_DATASTREAM_ID + " not found");
        }

        Set<Datapoint> datapoints = datastreamWithId.get().getDatapoints();
        assertNotNull(datapoints);
        assertEquals(1, datapoints.size());
        if (datapoints.stream().noneMatch(dp -> dp.getAt() == null && TEST_VALUE.equals(dp.getValue()))) {
            fail("Datapoint with null at and value " + TEST_VALUE + " not found");
        }
    }
}