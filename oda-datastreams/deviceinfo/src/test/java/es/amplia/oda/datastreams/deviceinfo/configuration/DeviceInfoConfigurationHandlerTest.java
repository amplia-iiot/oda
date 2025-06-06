package es.amplia.oda.datastreams.deviceinfo.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoConfigurationHandlerTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_API_KEY = "an-api-key";
    private static final String TEST_SOURCE  = "way-to-bundles";
    private static final String TEST_PATH = "way-to-scripts";
    private static final DeviceInfoConfiguration TEST_CONFIGURATION =
            new DeviceInfoConfiguration(TEST_DEVICE_ID, TEST_API_KEY, TEST_SOURCE, TEST_PATH, new HashMap<>());

    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";

    @Mock
    private DeviceInfoDatastreamsGetter mockedDeviceInfoDatastreamsGetter;
    @Mock
    private ScriptsLoader mockedScriptsLoader;
    @InjectMocks
    private DeviceInfoConfigurationHandler testHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(DeviceInfoConfigurationHandler.DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
        props.put(DeviceInfoConfigurationHandler.API_KEY_PROPERTY_NAME, TEST_API_KEY);
        props.put(DeviceInfoConfigurationHandler.SOURCE_PROPERTY_NAME, TEST_SOURCE);
        props.put(DeviceInfoConfigurationHandler.PATH_PROPERTY_NAME, TEST_PATH);

        testHandler.loadConfiguration(props);

        DeviceInfoConfiguration configuration =
                Whitebox.getInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME);

        assertEquals(TEST_DEVICE_ID, configuration.getDeviceId());
        assertEquals(TEST_API_KEY, configuration.getApiKey());
        assertEquals(TEST_SOURCE, configuration.getSource());
        assertEquals(TEST_PATH, configuration.getPath());
    }

    @Test
    public void testLoadConfigurationNoDeviceId() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(DeviceInfoConfigurationHandler.API_KEY_PROPERTY_NAME, TEST_API_KEY);
        props.put(DeviceInfoConfigurationHandler.SOURCE_PROPERTY_NAME, TEST_SOURCE);
        props.put(DeviceInfoConfigurationHandler.PATH_PROPERTY_NAME, TEST_PATH);

        testHandler.loadConfiguration(props);

        DeviceInfoConfiguration configuration =
                Whitebox.getInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME);

        assertEquals(TEST_API_KEY, configuration.getApiKey());
        assertEquals(TEST_SOURCE, configuration.getSource());
        assertEquals(TEST_PATH, configuration.getPath());
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingApiKey() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(DeviceInfoConfigurationHandler.SOURCE_PROPERTY_NAME, TEST_SOURCE);
        props.put(DeviceInfoConfigurationHandler.PATH_PROPERTY_NAME, TEST_PATH);

        testHandler.loadConfiguration(props);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingSource() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(DeviceInfoConfigurationHandler.API_KEY_PROPERTY_NAME, TEST_API_KEY);
        props.put(DeviceInfoConfigurationHandler.PATH_PROPERTY_NAME, TEST_PATH);

        testHandler.loadConfiguration(props);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingPathCommand() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(DeviceInfoConfigurationHandler.API_KEY_PROPERTY_NAME, TEST_API_KEY);
        props.put(DeviceInfoConfigurationHandler.SOURCE_PROPERTY_NAME, TEST_SOURCE);

        testHandler.loadConfiguration(props);

        fail("Configuration exception must be thrown");
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadDefaultConfiguration() {
        testHandler.loadDefaultConfiguration();
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        testHandler.applyConfiguration();

        verify(mockedScriptsLoader).load(eq(TEST_SOURCE), eq(TEST_PATH));
        verify(mockedDeviceInfoDatastreamsGetter).loadConfiguration(eq(TEST_CONFIGURATION));
    }
}