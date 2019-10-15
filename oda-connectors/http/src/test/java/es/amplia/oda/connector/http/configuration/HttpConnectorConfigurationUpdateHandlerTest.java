package es.amplia.oda.connector.http.configuration;

import es.amplia.oda.connector.http.HttpConnector;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static es.amplia.oda.connector.http.configuration.HttpConnectorConfigurationUpdateHandler.*;
import static es.amplia.oda.connector.http.configuration.ConnectorConfiguration.*;

@RunWith(MockitoJUnitRunner.class)
public class HttpConnectorConfigurationUpdateHandlerTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 1234;
    private static final String TEST_GENERAL_PATH = "general/path";
    private static final String TEST_COLLECTION_PATH = "collection/path";
    private static final boolean TEST_COMPRESSION_ENABLED = true;
    private static final int TEST_COMPRESSION_THRESHOLD = 1024;
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().host(TEST_HOST).port(TEST_PORT).generalPath(TEST_GENERAL_PATH)
                    .collectionPath(TEST_COLLECTION_PATH).compressionEnabled(TEST_COMPRESSION_ENABLED)
                    .compressionThreshold(TEST_COMPRESSION_THRESHOLD).build();

    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";

    @Mock
    private HttpConnector mockedConnector;
    @InjectMocks
    private HttpConnectorConfigurationUpdateHandler testConfigHandler;

    private Dictionary<String, String> createDictionary() {
        return new Hashtable<>();
    }

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = createDictionary();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(PORT_PROPERTY_NAME, Integer.toString(TEST_PORT));
        props.put(GENERAL_PATH_PROPERTY_NAME, TEST_GENERAL_PATH);
        props.put(COLLECTION_PATH_PROPERTY_NAME, TEST_COLLECTION_PATH);
        props.put(COMPRESSION_ENABLED_PROPERTY_NAME, Boolean.toString(TEST_COMPRESSION_ENABLED));
        props.put(COMPRESSION_THRESHOLD_PROPERTY_NAME, Integer.toString(TEST_COMPRESSION_THRESHOLD));

        testConfigHandler.loadConfiguration(props);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testLoadConfigurationDefaultValues() {
        Dictionary<String, String> props = createDictionary();
        props.put(HOST_PROPERTY_NAME, TEST_HOST);
        props.put(GENERAL_PATH_PROPERTY_NAME, TEST_GENERAL_PATH);
        props.put(COLLECTION_PATH_PROPERTY_NAME, TEST_COLLECTION_PATH);

        testConfigHandler.loadConfiguration(props);

        ConnectorConfiguration configuration = (ConnectorConfiguration)
                Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME);
        assertEquals(TEST_HOST, configuration.getHost());
        assertEquals(DEFAULT_PORT, configuration.getPort());
        assertEquals(TEST_GENERAL_PATH, configuration.getGeneralPath());
        assertEquals(TEST_COLLECTION_PATH, configuration.getCollectionPath());
        assertEquals(DEFAULT_COMPRESSION_ENABLED, configuration.isCompressionEnabled());
        assertEquals(DEFAULT_COMPRESSION_THRESHOLD, configuration.getCompressionThreshold());
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadConfiguration(eq(TEST_CONFIGURATION));
    }

    @Test
    public void testApplyConfigurationNoConfiguration() {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, null);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector, never()).loadConfiguration(any(ConnectorConfiguration.class));
    }
}