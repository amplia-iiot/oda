package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
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
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenGateEventDispatcherTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_HOST_ID = "host";
    private static final String[] TEST_PATH = new String[]{"deviceA", "deviceB"};
    private static final String[] TEST_PATH_WITH_HOST = new String[]{"host","deviceA", "deviceB"};
    private static final Long TEST_AT = System.currentTimeMillis();
    private static final int TEST_VALUE = 20;

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private Serializer serializer;
    @Mock
    private OpenGateConnector mockedConnector;
    @InjectMocks
    private OpenGateEventDispatcher testEventDispatcher;

    @Test
    public void testPublish() throws IOException {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output =
                new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH_WITH_HOST,
                        Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        when(serializer.serialize(any())).thenReturn(payload);

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(serializer).serialize(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }

    @Test
    public void testPublishPeriodicSentConfigured() {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);

        Whitebox.setInternalState(testEventDispatcher, "datastreamIdsConfigured",
                Collections.singletonList(TEST_DATASTREAM_ID));

        testEventDispatcher.publish(eventToTest);

        Map<String, List<Event>> collectedValues = Whitebox.getInternalState(testEventDispatcher, "collectedValues");
        List<Event> datastreamCollectedEvents = collectedValues.get(TEST_DATASTREAM_ID);
        assertNotNull(datastreamCollectedEvents);
        assertTrue(datastreamCollectedEvents.contains(eventToTest));
    }

    @Test
    public void testPublishWithNullHostIdAndNullPath() throws IOException {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, null,
                Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);
        when(serializer.serialize(any())).thenReturn(payload);

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(serializer).serialize(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }

    @Test
    public void testPublishWithEmptyDeviceIdAndNullPath() throws IOException {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, "", null, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, null,
                Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(serializer.serialize(any())).thenReturn(payload);

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(serializer).serialize(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }

    @Test
    public void testPublishWithDifferentDeviceIdAndHostIdAndNullPath() throws IOException {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output = new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, new String[] { TEST_HOST_ID },
                Collections.singleton(datastream));
        byte[] payload = new byte[]{1, 2, 3, 4};

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        when(serializer.serialize(any())).thenReturn(payload);

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(serializer).serialize(eq(output));
        verify(mockedConnector).uplink(eq(payload));
    }

    @Test
    public void testPublishSerializeExceptionCaught() throws IOException {
        Event eventToTest = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);
        Datapoint datapoint = new Datapoint(TEST_AT, TEST_VALUE);
        Datastream datastream = new Datastream(TEST_DATASTREAM_ID, Collections.singleton(datapoint));
        OutputDatastream output =
                new OutputDatastream(OPENGATE_VERSION, TEST_DEVICE_ID, TEST_PATH_WITH_HOST,
                        Collections.singleton(datastream));

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_ID);
        when(serializer.serialize(any())).thenThrow(new IOException());

        testEventDispatcher.publish(eventToTest);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(serializer).serialize(eq(output));
        verifyZeroInteractions(mockedConnector);
    }

    @Test
    public void testSetDatastreamIdsConfiguredLastConfigurationIsEmpty() {
        String datastreamId2 = "testDatastream2";
        String datastreamId3 = "testDatastream3";
        Set<String> datastreams1 = Collections.singleton(TEST_DATASTREAM_ID);
        Set<String> datastreams2 = Collections.singleton(datastreamId2);
        Set<String> datastreams3 = Collections.singleton(datastreamId3);

        testEventDispatcher.setDatastreamIdsConfigured(Arrays.asList(datastreams1, datastreams2, datastreams3));

        List<String> configuredDatastreams = Whitebox.getInternalState(testEventDispatcher, "datastreamIdsConfigured");
        assertEquals(3, configuredDatastreams.size());
        assertTrue(configuredDatastreams.contains(TEST_DATASTREAM_ID));
        assertTrue(configuredDatastreams.contains(datastreamId2));
        assertTrue(configuredDatastreams.contains(datastreamId3));
    }

    @Test
    public void testSetDatastreamIdsConfiguredLastConfigurationIsNotEmpty() {
        List<String> lastConfiguration = new ArrayList<>();
        lastConfiguration.add(TEST_DATASTREAM_ID);
        String datastreamId2 = "testDatastream2";
        String datastreamId3 = "testDatastream3";
        Set<String> datastreams2 = Collections.singleton(datastreamId2);
        Set<String> datastreams3 = Collections.singleton(datastreamId3);

        Whitebox.setInternalState(testEventDispatcher, "datastreamIdsConfigured", lastConfiguration);

        testEventDispatcher.setDatastreamIdsConfigured(Arrays.asList(datastreams2, datastreams3));

        List<String> configuredDatastreams = Whitebox.getInternalState(testEventDispatcher, "datastreamIdsConfigured");
        assertEquals(2, configuredDatastreams.size());
        assertFalse(configuredDatastreams.contains(TEST_DATASTREAM_ID));
        assertTrue(configuredDatastreams.contains(datastreamId2));
        assertTrue(configuredDatastreams.contains(datastreamId3));
    }

    @Test
    public void testGetAndCleanCollectedValues() {
        Event collectedEvent = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE);
        Map<String, List<Event>> collectedValues = new HashMap<>();
        collectedValues.put(TEST_DATASTREAM_ID, Collections.singletonList(collectedEvent));

        Whitebox.setInternalState(testEventDispatcher, "collectedValues", collectedValues);

        List<Event> result = testEventDispatcher.getAndCleanCollectedValues(TEST_DATASTREAM_ID);

        assertNotNull(result);
        assertEquals(Collections.singletonList(collectedEvent), result);
        assertFalse(collectedValues.containsKey(TEST_DATASTREAM_ID));
    }

    @Test
    public void testGetAndCleanCollectedValuesNoCollectedDatastream() {
        List<Event> result = testEventDispatcher.getAndCleanCollectedValues(TEST_DATASTREAM_ID);

        assertNull(result);
    }
}