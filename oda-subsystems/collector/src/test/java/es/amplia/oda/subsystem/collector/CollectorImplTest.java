package es.amplia.oda.subsystem.collector;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CollectorImplTest {

    private static final DevicePattern TEST_DEVICE_PATTERN = new DevicePattern("*");
    private static final String TEST_DEVICE_ID_1 = "testDevice";
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final String TEST_DATASTREAM_ID_1 = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_DATASTREAM_ID_3 = "testDatastream3";
    private static final long TEST_AT_1 = 1L;
    private static final long TEST_AT_2 = 2L;
    private static final long TEST_AT_3 = 3L;
    private static final Object TEST_VALUE_1 = "test";
    private static final Object TEST_VALUE_2 = 5;
    private static final Object TEST_VALUE_3 = 99.99;
    private static final Set<String> TEST_DATASTREAMS = new HashSet<>(Arrays.asList(
            TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3));

    @Mock
    private StateManager mockedStateManager;
    @Mock
    private EventDispatcher mockedEventDispatcher;
    @InjectMocks
    private CollectorImpl testCollector;

    @Captor
    private ArgumentCaptor<List<Event>> eventCaptor;


    @Test
    public void testCollect() {
        DatastreamValue dv1 =
                new DatastreamValue(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_1, TEST_AT_1, TEST_VALUE_1, Status.OK, null, false);
        DatastreamValue dv2 =
                new DatastreamValue(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_2, TEST_AT_2, TEST_VALUE_2, Status.OK, null, false);
        DatastreamValue dv3 =
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, TEST_AT_2, TEST_VALUE_2, Status.OK, null, false);
        DatastreamValue dv4 =
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_3, TEST_AT_3, TEST_VALUE_3, Status.OK, null, false);

        when(mockedStateManager.getDatastreamsInformation(any(DevicePattern.class), anySetOf(String.class)))
                .thenReturn(CompletableFuture.completedFuture(new HashSet<>(Arrays.asList(dv1, dv2, dv3, dv4))));

        testCollector.collect(TEST_DEVICE_PATTERN, TEST_DATASTREAMS);

        verify(mockedStateManager).getDatastreamsInformation(eq(TEST_DEVICE_PATTERN), eq(TEST_DATASTREAMS));
        verify(mockedEventDispatcher, times(1)).publish(eventCaptor.capture());
        List<Event> events = eventCaptor.getValue();
        assertPublishedEvent(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_1, TEST_AT_1, TEST_VALUE_1, events);
        assertPublishedEvent(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_2, TEST_AT_2, TEST_VALUE_2, events);
        assertPublishedEvent(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, TEST_AT_2, TEST_VALUE_2, events);
        assertPublishedEvent(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_3, TEST_AT_3, TEST_VALUE_3, events);
    }

    private void assertPublishedEvent(String deviceId, String datastreamId, long at, Object value, List<Event> events) {
        if (events.stream().noneMatch(event -> deviceId.equals(event.getDeviceId()) &&
                datastreamId.equals(event.getDatastreamId()) && at == event.getAt() && value.equals(event.getValue()))) {
            fail("No event published with deviceId " + deviceId + ", datastreamId " + datastreamId + ", at " + at +
                    "and value" + value);
        }
    }
}