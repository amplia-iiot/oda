package es.amplia.oda.operation.localprotocoldiscovery.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.MapBasedDictionary;
import es.amplia.oda.operation.localprotocoldiscovery.OperationLocalProtocolDiscoveryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;

import static es.amplia.oda.operation.localprotocoldiscovery.configuration.LocalProtocolDiscoveryConfigurationUpdateHandler.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LocalProtocolDiscoveryConfigurationUpdateHandlerTest {

    private static final String TEST_SERVER_URI = "http://test.server:1883";
    private static final String TEST_CLIENT_ID = "testClient";
    private static final String TEST_TOPIC = "test/topic";
    private static final LocalProtocolDiscoveryConfiguration TEST_CONFIGURATION =
            new LocalProtocolDiscoveryConfiguration(TEST_SERVER_URI, TEST_CLIENT_ID, TEST_TOPIC);
    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";


    @Mock
    private OperationLocalProtocolDiscoveryImpl mockedLocalProtocolDiscovery;
    @InjectMocks
    private LocalProtocolDiscoveryConfigurationUpdateHandler testConfigurationHandler;


    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(SERVER_URI_PROPERTY_NAME, TEST_SERVER_URI);
        testProps.put(CLIENT_ID_PROPERTY_NAME, TEST_CLIENT_ID);
        testProps.put(DISCOVERY_TOPIC_PROPERTY_NAME, TEST_TOPIC);

        testConfigurationHandler.loadConfiguration(testProps);

        assertEquals(TEST_CONFIGURATION,
                Whitebox.getInternalState(testConfigurationHandler, CURRENT_CONFIGURATION_FIELD_NAME));
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingServerUri() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(CLIENT_ID_PROPERTY_NAME, TEST_CLIENT_ID);
        testProps.put(DISCOVERY_TOPIC_PROPERTY_NAME, TEST_TOPIC);

        testConfigurationHandler.loadConfiguration(testProps);
    }

    @Test
    public void testLoadConfigurationMissingClientId() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(SERVER_URI_PROPERTY_NAME, TEST_SERVER_URI);
        testProps.put(DISCOVERY_TOPIC_PROPERTY_NAME, TEST_TOPIC);

        testConfigurationHandler.loadConfiguration(testProps);

        LocalProtocolDiscoveryConfiguration conf = (LocalProtocolDiscoveryConfiguration)
                Whitebox.getInternalState(testConfigurationHandler, CURRENT_CONFIGURATION_FIELD_NAME);
        assertEquals(TEST_SERVER_URI, conf.getServerURI());
        assertEquals(TEST_TOPIC, conf.getDiscoverTopic());
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingDiscoveryTopic() {
        Dictionary<String, String> testProps = new MapBasedDictionary<>(String.class);
        testProps.put(SERVER_URI_PROPERTY_NAME, TEST_SERVER_URI);
        testProps.put(CLIENT_ID_PROPERTY_NAME, TEST_CLIENT_ID);

        testConfigurationHandler.loadConfiguration(testProps);
    }

    @Test
    public void testApplyConfiguration() {
        Whitebox.setInternalState(testConfigurationHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        testConfigurationHandler.applyConfiguration();

        verify(mockedLocalProtocolDiscovery).loadConfiguration(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID), eq(TEST_TOPIC));
    }
}