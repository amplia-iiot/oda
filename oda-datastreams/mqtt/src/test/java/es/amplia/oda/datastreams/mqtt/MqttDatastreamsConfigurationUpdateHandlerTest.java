package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreamsConfigurationUpdateHandler.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsConfigurationUpdateHandlerTest {

    private static final String TEST_SERVER_URI = "tcp:://test.uri.com";
    private static final String TEST_CLIENT_ID = "testClient";
    private static final String TEST_ENABLE_TOPIC = "test/enable";
    private static final String TEST_DISABLE_TOPIC = "test/disable";
    private static final String TEST_EVENT_TOPIC = "test/event";
    private static final String TEST_READ_REQUEST_TOPIC = "test/read/request";
    private static final String TEST_READ_RESPONSE_TOPIC = "test/read/response";
    private static final String TEST_WRITE_REQUEST_TOPIC = "test/write/request";
    private static final String TEST_WRITE_RESPONSE_TOPIC = "test/write/response";
    private static final String TEST_LWT_TOPIC = "test/lwt";
    private static final MqttDatastreamsConfiguration TEST_CONFIGURATION =
            new MqttDatastreamsConfiguration(TEST_SERVER_URI, TEST_CLIENT_ID, TEST_ENABLE_TOPIC, TEST_DISABLE_TOPIC,
                    TEST_EVENT_TOPIC, TEST_READ_REQUEST_TOPIC, TEST_READ_RESPONSE_TOPIC, TEST_WRITE_REQUEST_TOPIC,
                    TEST_WRITE_RESPONSE_TOPIC, TEST_LWT_TOPIC);
    private static final String TEST_DATASTREAM_ID_1 = "testDatastream1";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_DEVICE_ID_1 = "testDevice1";
    private static final String TEST_DEVICE_ID_2 = "testDevice2";

    @Mock
    private MqttDatastreamsOrchestrator mockedOrchestrator;
    @InjectMocks
    private MqttDatastreamsConfigurationUpdateHandler testConfigHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put(SERVER_URI_PROPERTY_NAME, TEST_SERVER_URI);
        props.put(CLIENT_ID_PROPERTY_NAME, TEST_CLIENT_ID);
        props.put(ENABLE_DATASTREAM_TOPIC_PROPERTY_NAME, TEST_ENABLE_TOPIC);
        props.put(DISABLE_DATASTREAM_TOPIC_PROPERTY_NAME, TEST_DISABLE_TOPIC);
        props.put(EVENT_TOPIC_PROPERTY_NAME, TEST_EVENT_TOPIC);
        props.put(READ_REQUEST_TOPIC_PROPERTY_NAME, TEST_READ_REQUEST_TOPIC);
        props.put(READ_RESPONSE_TOPIC_PROPERTY_NAME, TEST_READ_RESPONSE_TOPIC);
        props.put(WRITE_REQUEST_TOPIC_PROPERTY_NAME, TEST_WRITE_REQUEST_TOPIC);
        props.put(WRITE_RESPONSE_TOPIC_PROPERTY_NAME, TEST_WRITE_RESPONSE_TOPIC);
        props.put(LWT_TOPIC_PROPERTY_NAME, TEST_LWT_TOPIC);
        props.put(TEST_DEVICE_ID_1 + ";" + TEST_DATASTREAM_ID_1, MqttDatastreamPermission.RD.toString());
        props.put(TEST_DEVICE_ID_1 + ";" + TEST_DATASTREAM_ID_2, MqttDatastreamPermission.WR.toString());
        props.put(TEST_DEVICE_ID_2 + ";" + TEST_DATASTREAM_ID_2, MqttDatastreamPermission.RW.toString());

        testConfigHandler.loadConfiguration(props);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
        List<DatastreamInfoWithPermission> datastreamsConfig = getInitialDatastreamsConfiguration();
        assertEquals(3, datastreamsConfig.size());
        assertTrue(datastreamsConfig.contains(
                new DatastreamInfoWithPermission(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_1, MqttDatastreamPermission.RD)));
        assertTrue(datastreamsConfig.contains(
                new DatastreamInfoWithPermission(TEST_DEVICE_ID_1, TEST_DATASTREAM_ID_2, MqttDatastreamPermission.WR)));
        assertTrue(datastreamsConfig.contains(
                new DatastreamInfoWithPermission(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, MqttDatastreamPermission.RW)));
    }

    @SuppressWarnings("unchecked")
    private List<DatastreamInfoWithPermission> getInitialDatastreamsConfiguration() {
        return (List<DatastreamInfoWithPermission>) Whitebox.getInternalState(testConfigHandler, "initialDatastreamsConfiguration");
    }

    @Test
    public void testApplyConfiguration() throws MqttException {
        List<DatastreamInfoWithPermission> dummyDatastreamsConfiguration = Collections.emptyList();

        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", TEST_CONFIGURATION);
        Whitebox.setInternalState(testConfigHandler, "initialDatastreamsConfiguration", dummyDatastreamsConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedOrchestrator).loadConfiguration(eq(TEST_CONFIGURATION), eq(dummyDatastreamsConfiguration));
    }
}