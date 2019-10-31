package es.amplia.oda.hardware.comms.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.MapBasedDictionary;
import es.amplia.oda.hardware.comms.CommsManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;

import static es.amplia.oda.hardware.comms.configuration.CommsConfigurationUpdateHandler.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CommsConfigurationUpdateHandlerTest {

    private static final String TEST_PIN = "1111";
    private static final String TEST_APN = "apn";
    private static final String TEST_USERNAME = "username";
    private static final String TEST_PASS = "testPass";
    private static final int TEST_CONNECTION_TIMEOUT = 15;
    private static final long TEST_RETRY_CONNECTION_TIMER = 30;
    private static final CommsConfiguration TEST_CONFIGURATION = new CommsConfiguration(TEST_PIN, TEST_APN,
            TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT, TEST_RETRY_CONNECTION_TIMER);
    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";


    @Mock
    private CommsManager mockedCommsManager;
    @InjectMocks
    private CommsConfigurationUpdateHandler testConfigHandler;

    @Test(expected = ConfigurationException.class)
    public void testLoadDefaultConfiguration() {
        testConfigHandler.loadDefaultConfiguration();
    }

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(PIN_PROPERTY_NAME, TEST_PIN);
        testProps.put(APN_PROPERTY_NAME, TEST_APN);
        testProps.put(USERNAME_PROPERTY_NAME, TEST_USERNAME);
        testProps.put(PASS_PROPERTY_NAME, TEST_PASS);
        testProps.put(CONNECTION_TIMEOUT_PROPERTY_NAME, Integer.toString(TEST_CONNECTION_TIMEOUT));
        testProps.put(RETRY_CONNECTION_TIMER_PROPERTY_NAME, Long.toString(TEST_RETRY_CONNECTION_TIMER));

        testConfigHandler.loadConfiguration(testProps);

        assertEquals(TEST_CONFIGURATION, getCurrentConfiguration());
    }

    @Test
    public void testLoadConfigurationDefaultValues() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(APN_PROPERTY_NAME, TEST_APN);

        testConfigHandler.loadConfiguration(testProps);

        CommsConfiguration conf = getCurrentConfiguration();
        assertEquals("", conf.getPin());
        assertEquals(TEST_APN, conf.getApn());
        assertEquals("", conf.getUsername());
        assertEquals("", conf.getPassword());
        assertEquals(DEFAULT_CONNECTION_TIMEOUT, conf.getConnectionTimeout());
        assertEquals(DEFAULT_RETRY_CONNECTION_TIMER, conf.getRetryConnectionTimer());
    }

    private CommsConfiguration getCurrentConfiguration() {
        return (CommsConfiguration) Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationMissingRequiredPropertyAPN() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(PIN_PROPERTY_NAME, TEST_PIN);
        testProps.put(USERNAME_PROPERTY_NAME, TEST_USERNAME);
        testProps.put(PASS_PROPERTY_NAME, TEST_PASS);

        testConfigHandler.loadConfiguration(testProps);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testProps, CURRENT_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        testConfigHandler.applyConfiguration();

        verify(mockedCommsManager).connect(eq(TEST_PIN), eq(TEST_APN), eq(TEST_USERNAME), eq(TEST_PASS),
                eq(TEST_CONNECTION_TIMEOUT), eq(TEST_RETRY_CONNECTION_TIMER));
    }
}