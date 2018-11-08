package es.amplia.oda.connector.thingstream.configuration;

import org.junit.Test;

import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.*;
import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.DEFAULT_MQTT_PORT;
import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.QOS.QOS_0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConnectorConfigurationBuilderTest {

    private static final ClientType CLIENT_TYPE = ClientType.MQTT;
    private static final String TOPIC = "test/topic";
    private static final String OPERATION_TOPIC = "test/operation/topic";
    private static final QOS QOS = QOS_0;
    private static final String CLIENT_ID = "testClientId";
    private static final int SHORT_CODE = 123;
    private static final String MQTT_HOST = "http://testhost.com";
    private static final int MQTT_PORT = 1234;
    private static final String MQTT_CLIENT_ID = "testMqttClientId";
    private static final String MQTT_USERNAME = "testUser";
    private static final String MQTT_PASSWORD = "testPassword";

    private static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE = "Illegal argument exception must be thrown";

    private final ConnectorConfigurationBuilder testBuilder = ConnectorConfiguration.builder();

    @Test
    public void testBuild() {
        ConnectorConfiguration configuration = testBuilder.clientType(CLIENT_TYPE)
                                                            .dataTopic(TOPIC)
                                                            .operationTopic(OPERATION_TOPIC)
                                                            .qos(QOS)
                                                            .clientId(CLIENT_ID)
                                                            .shortCode(SHORT_CODE)
                                                            .mqttHost(MQTT_HOST)
                                                            .mqttPort(MQTT_PORT)
                                                            .mqttClientId(MQTT_CLIENT_ID)
                                                            .mqttUsername(MQTT_USERNAME)
                                                            .mqttPassword(MQTT_PASSWORD)
                                                            .build();

        assertEquals(CLIENT_TYPE, configuration.getClientType());
        assertEquals(TOPIC, configuration.getDataTopic());
        assertEquals(OPERATION_TOPIC, configuration.getOperationTopic());
        assertEquals(QOS, configuration.getQos());
        assertEquals(CLIENT_ID, configuration.getClientId());
        assertEquals(SHORT_CODE, configuration.getShortCode());
        assertEquals(MQTT_HOST, configuration.getMqttHost());
        assertEquals(MQTT_PORT, configuration.getMqttPort());
        assertEquals(MQTT_USERNAME, configuration.getMqttUsername());
        assertEquals(MQTT_PASSWORD, configuration.getMqttPassword());
    }

    @Test
    public void testBuildDefaultValues() {
        ConnectorConfiguration configuration = testBuilder.clientType(CLIENT_TYPE)
                .dataTopic(TOPIC)
                .qos(QOS)
                .clientId(CLIENT_ID)
                .shortCode(SHORT_CODE)
                .mqttHost(MQTT_HOST)
                .mqttClientId(MQTT_CLIENT_ID)
                .mqttUsername(MQTT_USERNAME)
                .mqttPassword(MQTT_PASSWORD)
                .build();

        assertEquals("", configuration.getOperationTopic());
        assertEquals(DEFAULT_MQTT_PORT, configuration.getMqttPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingMandatoryConfiguration() {
        testBuilder.clientType(ClientType.MQTTSN)
                .qos(QOS)
                .clientId(CLIENT_ID)
                .shortCode(SHORT_CODE)
                .build();

        fail(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildIncompleteMQTTSNConfiguration() {
        testBuilder.clientType(ClientType.MQTTSN)
                .dataTopic(TOPIC)
                .qos(QOS)
                .clientId(CLIENT_ID)
                .build();

        fail(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildIncompleteMQTTConfiguration() {
        testBuilder.clientType(ClientType.MQTT)
                .dataTopic(TOPIC)
                .qos(QOS)
                .mqttHost(MQTT_HOST)
                .mqttClientId(MQTT_CLIENT_ID)
                .mqttUsername(MQTT_USERNAME)
                .build();

        fail(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
    }
}