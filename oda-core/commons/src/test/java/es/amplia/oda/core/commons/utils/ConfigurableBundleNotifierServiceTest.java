package es.amplia.oda.core.commons.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurableBundleNotifierServiceTest {

    @Mock
    private ConfigurationUpdateHandler mockedHandler;
    @Mock
    private EventAdmin mockedEventAdmin;
    @Mock
    private ServiceRegistration<?> mockedServiceRegistration;

    private ConfigurableBundleNotifierService testConfigurableBundleService;

    private static final Dictionary<String, Object> mockedProps = new Hashtable<>();
    
    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    private static final String messageProp = ConfigurableBundleNotifierService.MESSAGE_PROPERTY_NAME;

    private static final String baseTopic = ConfigurableBundleNotifierService.CONFIGURATION_EVENT_BASE_TOPIC;

    private static final String expectedUpdatedTopic = baseTopic + ConfigurableBundleNotifierService.CONFIGURATION_UPDATED_EVENT;
    private static final String expectedUpdatedMessage = ConfigurableBundleNotifierService.CONFIGURATION_UPDATED_MESSAGE;

    private static final String expectedDeletedTopic = baseTopic + ConfigurableBundleNotifierService.CONFIGURATION_DELETED_EVENT;
    private static final String expectedDeletedMessage = ConfigurableBundleNotifierService.CONFIGURATION_DELETED_MESSAGE;

    private static final String expectedErrorTopic = baseTopic + ConfigurableBundleNotifierService.CONFIGURATION_ERROR_EVENT;

    @Before
    public void setUp() {
        List<ServiceRegistration<?>> serviceRegistrations =
                java.util.Collections.singletonList(mockedServiceRegistration);
        testConfigurableBundleService =new ConfigurableBundleNotifierService("testConfigService", mockedHandler,
                mockedEventAdmin, serviceRegistrations);
    }

    @Test
    public void testDeleteConfiguration() throws Exception {
        testConfigurableBundleService.updated(null);

        verify(mockedHandler).loadDefaultConfiguration();
        verify(mockedHandler, never()).loadConfiguration(any());
        verify(mockedServiceRegistration).setProperties(null);
        verify(mockedEventAdmin).postEvent(eventCaptor.capture());
        assertEquals(expectedDeletedTopic, eventCaptor.getValue().getTopic());
        assertEquals(expectedDeletedMessage, eventCaptor.getValue().getProperty(messageProp));
    }

    @Test (expected = ConfigurationException.class)
    public void testDeleteConfigurationException() throws Exception {
        doThrow(Exception.class).when(mockedHandler).loadDefaultConfiguration();

        try {
            testConfigurableBundleService.updated(null);
        } finally {
            verify(mockedEventAdmin).postEvent(eventCaptor.capture());
            assertEquals(expectedErrorTopic, eventCaptor.getValue().getTopic());
        }
    }

    @Test
    public void testUpdateConfiguration() throws Exception {
        testConfigurableBundleService.updated(mockedProps);

        verify(mockedHandler).loadConfiguration(any());
        verify(mockedHandler, never()).loadDefaultConfiguration();
        verify(mockedServiceRegistration).setProperties(eq(mockedProps));
        verify(mockedEventAdmin).postEvent(eventCaptor.capture());
        assertEquals(expectedUpdatedTopic, eventCaptor.getValue().getTopic());
        assertEquals(expectedUpdatedMessage, eventCaptor.getValue().getProperty(messageProp));
    }

    @Test(expected = ConfigurationException.class)
    public void testUpdateConfigurationException() throws Exception {
        doThrow(Exception.class).when(mockedHandler).loadConfiguration(eq(mockedProps));

        try {
            testConfigurableBundleService.updated(mockedProps);
        } finally {
            verify(mockedEventAdmin).postEvent(eventCaptor.capture());
            assertEquals(expectedErrorTopic, eventCaptor.getValue().getTopic());
        }
    }

    /*
     * When no EventAdmin is given, the system should be properly configured without sending the notification
     */
    @Test
    public void testUpdateConfigurationNotEventAdmin() throws Exception {
        ConfigurableBundleNotifierService notEventAdminTest =
                new ConfigurableBundleNotifierService(null, mockedHandler, null);

        notEventAdminTest.updated(mockedProps);

        verify(mockedHandler).loadConfiguration(any());
        verify(mockedHandler, never()).loadDefaultConfiguration();
    }

    /*
     * When EventAdmin throw an exception, the system should be properly configured without sending the notification
     */
    @Test
    public void testUpdateConfigurationEventAdminException() throws Exception {
        doThrow(Exception.class).when(mockedEventAdmin).postEvent(any(Event.class));

        testConfigurableBundleService.updated(mockedProps);

        verify(mockedHandler).loadConfiguration(any());
        verify(mockedHandler, never()).loadDefaultConfiguration();
        verify(mockedEventAdmin).postEvent(any(Event.class));
    }
}