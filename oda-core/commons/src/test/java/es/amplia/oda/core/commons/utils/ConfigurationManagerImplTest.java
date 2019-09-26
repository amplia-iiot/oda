package es.amplia.oda.core.commons.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Optional;

import static es.amplia.oda.core.commons.utils.ConfigurationManagerImpl.FILENAME_KEY;
import static es.amplia.oda.core.commons.utils.ConfigurationManagerImpl.ODA_BUNDLES_CONFIGURATION_FOLDER_PROPERTY_NAME;

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
    private static final String TEST_BUNDLES_FOLDER = "file:/test/deploy";
    private static final String TEST_BUNDLE_LOCATION = "file:/test/deploy/test.jar";
    private static final String TEST_CONFIGURATION_FOLDER = "file:/test/config/folder";
    private static final String TEST_CONFIGURATION_FILE = TEST_CONFIGURATION_FOLDER + "/" + TEST_PID + ".cfg";

    @Mock
    private ConfigurationAdmin mockedConfigurationAdmin;

    private ConfigurationManagerImpl testConfigurationManager;

    @Mock
    private Configuration mockedConfiguration;
    @Captor
    private ArgumentCaptor<Dictionary<String, Object>> propsCaptor;


    @Before
    public void setUp() {
        System.setProperty(ODA_BUNDLES_CONFIGURATION_FOLDER_PROPERTY_NAME, TEST_CONFIGURATION_FOLDER);

        testConfigurationManager = new ConfigurationManagerImpl(mockedConfigurationAdmin);
    }

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
    public void testUpdateConfigurationPropertiesDoesNotExistConfigFolderSetByEnvironment() throws IOException {
        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(null);

        testConfigurationManager.updateConfiguration(TEST_PID, TEST_PROPERTY, TEST_VALUE_UPDATED);

        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(propsCaptor.capture());
        Dictionary<String, Object> props = propsCaptor.getValue();
        assertEquals(3, props.size());
        assertEquals(TEST_PID, props.get(Constants.SERVICE_PID));
        assertEquals(TEST_CONFIGURATION_FILE, props.get(FILENAME_KEY));
        assertEquals(TEST_VALUE_UPDATED, props.get(TEST_PROPERTY));
    }

    @Test
    public void testUpdateConfigurationPropertiesDoesNotExistDefaultConfigFolder() throws IOException {
        System.clearProperty(ODA_BUNDLES_CONFIGURATION_FOLDER_PROPERTY_NAME);
        ConfigurationManagerImpl defaultConfigFolderConfigManager =
                new ConfigurationManagerImpl(mockedConfigurationAdmin);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(null);
        when(mockedConfiguration.getBundleLocation()).thenReturn(TEST_BUNDLE_LOCATION);

        defaultConfigFolderConfigManager.updateConfiguration(TEST_PID, TEST_PROPERTY, TEST_VALUE_UPDATED);

        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(propsCaptor.capture());
        Dictionary<String, Object> props = propsCaptor.getValue();
        assertEquals(3, props.size());
        assertEquals(TEST_PID, props.get(Constants.SERVICE_PID));
        assertEquals(TEST_BUNDLES_FOLDER + "/../configuration/" + TEST_PID + ".cfg", props.get(FILENAME_KEY));
        assertEquals(TEST_VALUE_UPDATED, props.get(TEST_PROPERTY));
    }

    @Test
    public void testUpdateConfigurationProperties() throws IOException {
        String key1 = "key1";
        String value1 = "test1";
        String key2 = "key2";
        String value2 = "value2";
        HashMap<String, Object> newProperties = new HashMap<>();
        newProperties.put(key1, value1);
        newProperties.put(key2, value2);
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        testConfigurationManager.updateConfiguration(TEST_PID, newProperties);

        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(eq(props));
        assertEquals(3, props.size());
        assertEquals(TEST_VALUE, props.get(TEST_PROPERTY));
        assertEquals(value1, props.get(key1));
        assertEquals(value2, props.get(key2));
    }

    @Test
    public void testReplaceConfigurationProperties() throws IOException {
        String key1 = "key1";
        String value1 = "test1";
        String key2 = "key2";
        String value2 = "value2";
        HashMap<String, Object> newProperties = new HashMap<>();
        newProperties.put(key1, value1);
        newProperties.put(key2, value2);
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        testConfigurationManager.replaceConfiguration(TEST_PID, newProperties);

        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(eq(props));
        assertEquals(2, props.size());
        assertEquals(value1, props.get(key1));
        assertEquals(value2, props.get(key2));
    }

    @Test
    public void testClearConfiguration() throws IOException {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        testConfigurationManager.clearConfiguration(TEST_PID);

        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(propsCaptor.capture());
        Dictionary<String,Object> currentProps = propsCaptor.getValue();
        assertTrue(currentProps.isEmpty());
    }

    @Test
    public void testClearConfigurationDoNotRemoveFileInstallProperties() throws IOException {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TEST_PROPERTY, TEST_VALUE);
        props.put(Constants.SERVICE_PID, TEST_PID);
        props.put(FILENAME_KEY, TEST_CONFIGURATION_FILE);

        when(mockedConfigurationAdmin.getConfiguration(anyString())).thenReturn(mockedConfiguration);
        when(mockedConfiguration.getProperties()).thenReturn(props);

        testConfigurationManager.clearConfiguration(TEST_PID);

        verify(mockedConfigurationAdmin).getConfiguration(eq(TEST_PID));
        verify(mockedConfiguration).getProperties();
        verify(mockedConfiguration).update(propsCaptor.capture());
        Dictionary<String,Object> currentProps = propsCaptor.getValue();
        assertEquals(2, currentProps.size());
        assertEquals(TEST_PID, currentProps.get(Constants.SERVICE_PID));
        assertEquals(TEST_CONFIGURATION_FILE, currentProps.get(FILENAME_KEY));
        assertNull(currentProps.get(TEST_PROPERTY));
    }
}