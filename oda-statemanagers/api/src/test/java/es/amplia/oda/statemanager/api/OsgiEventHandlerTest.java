package es.amplia.oda.statemanager.api;

import es.amplia.oda.event.api.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.*;

import static es.amplia.oda.core.commons.utils.Events.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OsgiEventHandlerTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String[] TEST_PATH = new String[] { "gateway1", "gateway2" };
    private static final long TEST_AT = 123456789L;
    private static final double TEST_VALUE = 99.99;
    private static final String STATE_MANAGER_FIELD_NAME = "stateManager";

    @Mock
    private BundleContext mockedContext;
    @InjectMocks
    private OsgiEventHandler testEventHandler;

    @Captor
    private ArgumentCaptor<Dictionary<String, Object>> propertiesCaptor;
    @Captor
    private ArgumentCaptor<OsgiEventHandler.EventHandlerImpl> eventHandlerCaptor;

    @Mock
    private ServiceRegistration<EventHandler> mockedRegistration;
    @Mock
    private StateManager mockedStateManager;

    @Test
    public void testConstructor() {
        verify(mockedContext).registerService(eq(EventHandler.class), any(OsgiEventHandler.EventHandlerImpl.class),
                propertiesCaptor.capture());
        Dictionary<String, Object> capturedProperties = propertiesCaptor.getValue();
        assertArrayEquals(new String[] { EVENT_TOPIC }, (String[]) capturedProperties.get(EventConstants.EVENT_TOPIC));
    }

    @Test
    public void testHandleEvent() {
        Map<String, Object> event = new HashMap<>();
        List<Map<String, Object>> events = new ArrayList<>();
        Map<String, Object> props = new HashMap<>();
        props.put(DATASTREAM_ID_PROPERTY_NAME, TEST_DATASTREAM_ID);
        props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
        props.put(PATH_PROPERTY_NAME, TEST_PATH);
        props.put(AT_PROPERTY_NAME, TEST_AT);
        props.put(VALUE_PROPERTY_NAME, TEST_VALUE);
        events.add(props);
        event.put("events", events);
        org.osgi.service.event.Event osgiEvent = new org.osgi.service.event.Event(EVENT_TOPIC, event);
        List<Event> expectedEvent = Collections.singletonList(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, TEST_PATH, TEST_AT, TEST_VALUE));

        verify(mockedContext).registerService(eq(EventHandler.class), eventHandlerCaptor.capture(), any());

        Whitebox.setInternalState(testEventHandler, STATE_MANAGER_FIELD_NAME, mockedStateManager);

        OsgiEventHandler.EventHandlerImpl eventHandler = eventHandlerCaptor.getValue();
        eventHandler.handleEvent(osgiEvent);

        verify(mockedStateManager).onReceivedEvents(eq(expectedEvent));
    }

    @Test
    public void testRegisterStateManager() {
        testEventHandler.registerStateManager(mockedStateManager);

        assertEquals(mockedStateManager, Whitebox.getInternalState(testEventHandler, STATE_MANAGER_FIELD_NAME));
    }

    @Test
    public void testUnregisterStateManager() {
        Whitebox.setInternalState(testEventHandler, STATE_MANAGER_FIELD_NAME, mockedStateManager);

        testEventHandler.unregisterStateManager();

        assertNull(Whitebox.getInternalState(testEventHandler, STATE_MANAGER_FIELD_NAME));

    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testEventHandler, "registration", mockedRegistration);

        testEventHandler.close();

        verify(mockedRegistration).unregister();
    }
}