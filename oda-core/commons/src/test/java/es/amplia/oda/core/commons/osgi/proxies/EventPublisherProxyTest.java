package es.amplia.oda.core.commons.osgi.proxies;

import javafx.beans.binding.ObjectExpression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.core.commons.utils.Events.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EventPublisherProxy.class)
public class EventPublisherProxyTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String[] TEST_PATH = new String[] { "gateway1", "gateway2"};
    private static final long TEST_AT = 123456789L;
    private static final double TEST_VALUE = 99.99;
    private static final String TEST_VALUE_2 = "testing";

    @Mock
    private BundleContext mockedContext;

    private EventPublisherProxy testEventPublisher;

    @Mock
    private EventAdminProxy mockedEventAdmin;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(EventAdminProxy.class).withAnyArguments().thenReturn(mockedEventAdmin);

        testEventPublisher = new EventPublisherProxy(mockedContext);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(EventAdminProxy.class).withArguments(eq(mockedContext));
    }

    @Test
    public void testPublishEvent() {
        testEventPublisher.publishEvent(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_PATH, TEST_AT, TEST_VALUE);

        verify(mockedEventAdmin).sendEvent(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals(EVENT_TOPIC, capturedEvent.getTopic());
        assertEquals(TEST_DEVICE_ID, capturedEvent.getProperty(DEVICE_ID_PROPERTY_NAME));
        assertEquals(TEST_DATASTREAM_ID, capturedEvent.getProperty(DATASTREAM_ID_PROPERTY_NAME));
        assertEquals(TEST_PATH, capturedEvent.getProperty(PATH_PROPERTY_NAME));
        assertEquals(TEST_AT, capturedEvent.getProperty(AT_PROPERTY_NAME));
        assertEquals(TEST_VALUE, capturedEvent.getProperty(VALUE_PROPERTY_NAME));
    }

    @Test
    public void testPublishGroupEvents() {
        Map<String, Map<Long, Object>> events = new HashMap<>();
        Map<Long, Object> event1 = new HashMap<>();
        event1.put(TEST_AT, TEST_VALUE);
        Map<Long, Object> event2 = new HashMap<>();
        event2.put(null, TEST_VALUE_2);
        events.put(TEST_DATASTREAM_ID, event1);
        events.put(TEST_DATASTREAM_ID_2, event2);

        testEventPublisher.publishGroupEvents(TEST_DEVICE_ID, TEST_PATH, events);

        verify(mockedEventAdmin).sendEvent(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent.getProperty("events"));
        assertEquals(2, ((List) capturedEvent.getProperty("events")).size());
        Map<String, Object> event1Received = ((Map)((List) capturedEvent.getProperty("events")).get(0));
        assertEquals(TEST_DEVICE_ID, event1Received.get(DEVICE_ID_PROPERTY_NAME));
        assertEquals(TEST_PATH, event1Received.get(PATH_PROPERTY_NAME));
        assertEquals(TEST_DATASTREAM_ID, event1Received.get(DATASTREAM_ID_PROPERTY_NAME));
        assertEquals(TEST_AT, event1Received.get(AT_PROPERTY_NAME));
        assertEquals(TEST_VALUE, event1Received.get(VALUE_PROPERTY_NAME));
        Map<String, Object> event2Received = ((Map)((List) capturedEvent.getProperty("events")).get(1));
        assertEquals(TEST_DEVICE_ID, event2Received.get(DEVICE_ID_PROPERTY_NAME));
        assertEquals(TEST_PATH, event2Received.get(PATH_PROPERTY_NAME));
        assertEquals(TEST_DATASTREAM_ID_2, event2Received.get(DATASTREAM_ID_PROPERTY_NAME));
        assertNull(event2Received.get(AT_PROPERTY_NAME));
        assertEquals(TEST_VALUE_2, event2Received.get(VALUE_PROPERTY_NAME));
    }

    @Test
    public void testClose() {
        testEventPublisher.close();

        verify(mockedEventAdmin).close();
    }
}