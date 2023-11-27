package es.amplia.oda.datastreams.mqtt.configuration;

import es.amplia.oda.comms.mqtt.api.MqttException;

import es.amplia.oda.datastreams.mqtt.MqttDatastreamsOrchestrator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfigurationUpdateHandler.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsConfigurationUpdateHandlerTest {

    private static final String TEST_SERVER_URI = "tcp:://test.uri.com";
    private static final String TEST_CLIENT_ID = "testClient";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_EVENT_TOPIC = "test/event";
    private static final String TEST_REQUEST_TOPIC = "test/request";
    private static final String TEST_RESPONSE_TOPIC = "test/response";
    private static final int TEST_QOS = 1;
    private static final boolean TEST_RETAINED = false;
    private static final MqttDatastreamsConfiguration TEST_CONFIGURATION =
            new MqttDatastreamsConfiguration(TEST_SERVER_URI, TEST_CLIENT_ID, TEST_PASSWORD, TEST_EVENT_TOPIC,
                                            TEST_REQUEST_TOPIC, TEST_RESPONSE_TOPIC, TEST_QOS, TEST_RETAINED);

    @Mock
    private MqttDatastreamsOrchestrator mockedOrchestrator;
    @InjectMocks
    private MqttDatastreamsConfigurationUpdateHandler testConfigHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(SERVER_URI_PROPERTY_NAME, TEST_SERVER_URI);
        props.put(CLIENT_ID_PROPERTY_NAME, TEST_CLIENT_ID);
        props.put(PASSWORD_PROPERTY_NAME, TEST_PASSWORD);
        props.put(EVENT_TOPIC_PROPERTY_NAME, TEST_EVENT_TOPIC);
        props.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        props.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        testConfigHandler.loadConfiguration(props);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testApplyConfiguration() throws MqttException {
        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", TEST_CONFIGURATION);

        testConfigHandler.applyConfiguration();

        verify(mockedOrchestrator).loadConfiguration(eq(TEST_CONFIGURATION));
    }
}