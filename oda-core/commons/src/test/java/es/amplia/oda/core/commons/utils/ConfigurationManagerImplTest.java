package es.amplia.oda.core.commons.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationManagerImplTest {

    private static final String TEST_PID = "test.pid";
    private static final String TEST_PROPERTY = "testProp";
    private static final String TEST_VALUE = "testValue";
    private static final String TEST_VALUE_UPDATED = "testValueUpdated";


    @Mock
    private ConfigurationAdmin mockedConfigurationAdmin;
    @InjectMocks
    private ConfigurationManagerImpl testConfigurationManager;

    @Mock
    private Configuration mockedConfiguration;
    @Captor
    private ArgumentCaptor<Dictionary<String, Object>> propsCaptor;

    @Test
    public void testGetConfiguration() throws IOException {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        Optional<String> optionalValue = testConfigurationManager.getConfiguration(TEST_PID, TEST_PROPERTY);

        assertTrue(optionalValue.isPresent());
        assertEquals(TEST_VALUE, optionalValue.get());
        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
    }

    @Test
    public void testGetConfigurationPropertyDoesNotExist() throws IOException {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        Optional<String> optionalValue = testConfigurationManager.getConfiguration(TEST_PID, "otherProperty");

        assertFalse(optionalValue.isPresent());
        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
    }

    @Test
    public void testGetConfigurationPropertiesNotExist() throws IOException {
        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(null);

        Optional<String> optionalValue = testConfigurationManager.getConfiguration(TEST_PID, "otherProperty");

        assertFalse(optionalValue.isPresent());
        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
    }

    @Test
    public void testUpdateConfiguration() throws IOException {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        testConfigurationManager.updateConfiguration(TEST_PID, TEST_PROPERTY, TEST_VALUE_UPDATED);


        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(eq(props));
        assertEquals(TEST_VALUE_UPDATED, props.get(TEST_PROPERTY));
    }

    @Test
    public void testUpdateConfigurationPropertyDoesNotExist() throws IOException {
        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(null);

        testConfigurationManager.updateConfiguration(TEST_PID, TEST_PROPERTY, TEST_VALUE_UPDATED);


        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(propsCaptor.capture());
        Dictionary<String, Object> props = propsCaptor.getValue();
        assertEquals(1, props.size());
        assertEquals(TEST_VALUE_UPDATED, props.get(TEST_PROPERTY));
    }
}