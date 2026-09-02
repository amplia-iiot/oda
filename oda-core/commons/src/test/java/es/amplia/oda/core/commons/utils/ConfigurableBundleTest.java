package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.osgi.proxies.EventAdminProxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static es.amplia.oda.core.commons.utils.ConfigurableBundleImpl.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConfigurableBundleTest {

    private static final String EXPECTED_UPDATED_TOPIC = CONFIGURATION_EVENT_BASE_TOPIC + CONFIGURATION_UPDATED_EVENT;
    private static final String EXPECTED_DELETED_TOPIC = CONFIGURATION_EVENT_BASE_TOPIC + CONFIGURATION_DELETED_EVENT;
    private static final String EXPECTED_ERROR_TOPIC = CONFIGURATION_EVENT_BASE_TOPIC + CONFIGURATION_ERROR_EVENT;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ConfigurationUpdateHandler mockedHandler;

    private ConfigurableBundle testConfigurableBundle;

    private MockedConstruction<EventAdminProxy> eventAdminConstruction;
    private final List<List<?>> eventAdminArgs = new ArrayList<>();
    private EventAdminProxy mockedEventAdmin;
    @Mock
    private ServiceRegistration<?> mockedServiceRegistration;
    @Mock
    private ServiceRegistration<ManagedService> mockedRegistration;
    private static final Dictionary<String, Object> mockedProps = new Hashtable<>();
    @Captor
    private ArgumentCaptor<Event> eventCaptor;


    @Before
    public void setUp() throws Exception {
        Bundle mockedBundle = mock(Bundle.class);

        when(mockedContext.getBundle()).thenReturn(mockedBundle);
        String bundleName = "testBundle";
        when(mockedBundle.getSymbolicName()).thenReturn(bundleName);
        eventAdminConstruction = mockConstruction(EventAdminProxy.class,
                (mock, mctx) -> eventAdminArgs.add(new ArrayList<>(mctx.arguments())));
        List<ServiceRegistration<?>> serviceRegistrations =
                java.util.Collections.singletonList(mockedServiceRegistration);

        testConfigurableBundle = new ConfigurableBundleImpl(mockedContext, mockedHandler, serviceRegistrations);
        mockedEventAdmin = eventAdminConstruction.constructed().get(0);
    }

    @After
    public void tearDown() {
        eventAdminConstruction.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testConfigurableBundle);

        assertEquals(1, eventAdminConstruction.constructed().size());
        assertEquals(mockedContext, eventAdminArgs.get(0).get(0));
        verify(mockedContext).registerService(eq(ManagedService.class), eq(testConfigurableBundle), any());
    }

    @Test
    public void testDeleteConfiguration() throws Exception {
        testConfigurableBundle.updated(null);

        verify(mockedHandler).loadDefaultConfiguration();
        verify(mockedHandler, never()).loadConfiguration(any());
        verify(mockedServiceRegistration).setProperties(null);
        verify(mockedEventAdmin).postEvent(eventCaptor.capture());
        assertEquals(EXPECTED_DELETED_TOPIC, eventCaptor.getValue().getTopic());
        assertEquals(CONFIGURATION_DELETED_MESSAGE, eventCaptor.getValue().getProperty(MESSAGE_PROPERTY_NAME));
    }

    @Test (expected = ConfigurationException.class)
    public void testDeleteConfigurationException() throws Exception {
        doThrow(RuntimeException.class).when(mockedHandler).loadDefaultConfiguration();

        try {
            testConfigurableBundle.updated(null);
        } finally {
            verify(mockedEventAdmin).postEvent(eventCaptor.capture());
            assertEquals(EXPECTED_ERROR_TOPIC, eventCaptor.getValue().getTopic());
        }
    }

    @Test
    public void testUpdateConfiguration() throws Exception {
        testConfigurableBundle.updated(mockedProps);

        verify(mockedHandler).loadConfiguration(any());
        verify(mockedHandler, never()).loadDefaultConfiguration();
        verify(mockedServiceRegistration).setProperties(eq(mockedProps));
        verify(mockedEventAdmin).postEvent(eventCaptor.capture());
        assertEquals(EXPECTED_UPDATED_TOPIC, eventCaptor.getValue().getTopic());
        assertEquals(CONFIGURATION_UPDATED_MESSAGE, eventCaptor.getValue().getProperty(MESSAGE_PROPERTY_NAME));
    }

    @Test
    public void testUpdateExtraEntriesAreRemoved() throws Exception {
        mockedProps.put(Constants.SERVICE_PID, "testService");
        mockedProps.put(FILENAME_KEY, "test.service.configuration.filename.cfg");

        testConfigurableBundle.updated(mockedProps);

        verify(mockedHandler).loadConfiguration(any());
        verify(mockedHandler, never()).loadDefaultConfiguration();
        verify(mockedServiceRegistration).setProperties(eq(mockedProps));
        verify(mockedEventAdmin).postEvent(eventCaptor.capture());
        assertEquals(EXPECTED_UPDATED_TOPIC, eventCaptor.getValue().getTopic());
        assertEquals(CONFIGURATION_UPDATED_MESSAGE, eventCaptor.getValue().getProperty(MESSAGE_PROPERTY_NAME));
    }

    @Test(expected = ConfigurationException.class)
    public void testUpdateConfigurationException() throws Exception {
        doThrow(RuntimeException.class).when(mockedHandler).loadConfiguration(any());

        try {
            testConfigurableBundle.updated(mockedProps);
        } finally {
            verify(mockedEventAdmin).postEvent(eventCaptor.capture());
            assertEquals(EXPECTED_ERROR_TOPIC, eventCaptor.getValue().getTopic());
        }
    }

    @Test
    public void testPersistConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put("prop1", "value1");

        Whitebox.setInternalState(testConfigurableBundle, "configServiceRegistration", mockedRegistration);

        testConfigurableBundle.persistConfiguration(props);

        verify(mockedRegistration).setProperties(eq(props));
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testConfigurableBundle, "configServiceRegistration", mockedRegistration);

        testConfigurableBundle.close();

        verify(mockedRegistration).unregister();
        verify(mockedEventAdmin).close();
    }
}
