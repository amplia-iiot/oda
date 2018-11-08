package es.amplia.oda.connector.thingstream.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.connector.thingstream.ThingstreamConnector;

import com.myriadgroup.iot.sdk.client.message.MessageClientException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.connector.thingstream.configuration.ConfigurationUpdateHandlerImpl.*;
import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.ClientType;
import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.QOS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationUpdateHandlerImplTest {

    private static final String CLIENT_TYPE = "mqtt-sn";
    private static final String TOPIC = "test/topic";
    private static final String OPERATION_TOPIC = "test/operation/topic";
    private static final String QOS_STRING = "0";
    private static final String CLIENT_ID = "testClientId";
    private static final String SHORT_CODE = "123";
    private static final String MQTT_HOST = "http://testhost.com";
    private static final String MQTT_PORT = "1234";
    private static final String MQTT_CLIENT_ID = "testMqttClientId";
    private static final String MQTT_USERNAME = "testUser";
    private static final String MQTT_PASSWORD = "testPassword";

    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";
    private static final String CONFIGURATION_EXCEPTION_MESSAGE = "Configuration exception must be thrown";
    private static final String MQTT_SN_CLIENT_TYPE = "mqtt-sn";

    @Mock
    private ThingstreamConnector mockedConnector;
    @InjectMocks
    private ConfigurationUpdateHandlerImpl testConfigHandler;

    private final ConnectorConfiguration testConfiguration = ConnectorConfiguration.builder()
                                                                .clientType(ClientType.MQTTSN)
                                                                .dataTopic(TOPIC)
                                                                .qos(QOS.QOS_0)
                                                                .clientId(CLIENT_ID)
                                                                .shortCode(123)
                                                                .build();

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> testProps = new Hashtable<>();
        testProps.put(CLIENT_TYPE_PROPERTY_NAME, CLIENT_TYPE);
        testProps.put(DATA_TOPIC_PROPERTY_NAME, TOPIC);
        testProps.put(OPERATION_TOPIC_PROPERTY_NAME, OPERATION_TOPIC);
        testProps.put(QOS_PROPERTY_NAME, QOS_STRING);
        testProps.put(MQTTSN_CLIENT_ID_PROPERTY_NAME, CLIENT_ID);
        testProps.put(MQTTSN_SHORT_CODE_PROPERTY_NAME, SHORT_CODE);
        testProps.put(MQTT_HOST_PROPERTY_NAME, MQTT_HOST);
        testProps.put(MQTT_PORT_PROPERTY_NAME, MQTT_PORT);
        testProps.put(MQTT_PORT_PROPERTY_NAME, MQTT_PORT);
        testProps.put(MQTT_CLIENT_ID_PROPERTY_NAME, MQTT_CLIENT_ID);
        testProps.put(MQTT_USERNAME_PROPERTY_NAME, MQTT_USERNAME);
        testProps.put(MQTT_PASSWORD_PROPERTY_NAME, MQTT_PASSWORD);

        testConfigHandler.loadConfiguration(testProps);

        ConnectorConfiguration configuration = Whitebox.getInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME);

        assertEquals(ClientType.parse(CLIENT_TYPE), configuration.getClientType());
        assertEquals(TOPIC, configuration.getDataTopic());
        assertEquals(OPERATION_TOPIC, configuration.getOperationTopic());
        assertEquals(QOS.parse(QOS_STRING), configuration.getQos());
        assertEquals(CLIENT_ID, configuration.getClientId());
        assertEquals(Integer.parseInt(SHORT_CODE), configuration.getShortCode());
        assertEquals(MQTT_HOST, configuration.getMqttHost());
        assertEquals(Integer.parseInt(MQTT_PORT), configuration.getMqttPort());
        assertEquals(MQTT_USERNAME, configuration.getMqttUsername());
        assertEquals(MQTT_PASSWORD, configuration.getMqttPassword());
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadInvalidConfiguration() {
        Dictionary<String, String> testProps = new Hashtable<>();
        testProps.put(CLIENT_TYPE_PROPERTY_NAME, MQTT_SN_CLIENT_TYPE);
        testProps.put(DATA_TOPIC_PROPERTY_NAME, TOPIC);
        testProps.put(QOS_PROPERTY_NAME, QOS_STRING);
        testProps.put(MQTTSN_CLIENT_ID_PROPERTY_NAME, CLIENT_ID);

        testConfigHandler.loadConfiguration(testProps);

        fail(CONFIGURATION_EXCEPTION_MESSAGE);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadNumberFormatException() {
        Dictionary<String, String> testProps = new Hashtable<>();
        testProps.put(CLIENT_TYPE_PROPERTY_NAME, MQTT_SN_CLIENT_TYPE);
        testProps.put(DATA_TOPIC_PROPERTY_NAME, TOPIC);
        testProps.put(QOS_PROPERTY_NAME, QOS_STRING);
        testProps.put(MQTTSN_CLIENT_ID_PROPERTY_NAME, CLIENT_ID);
        testProps.put(MQTTSN_SHORT_CODE_PROPERTY_NAME, "invalid");

        testConfigHandler.loadConfiguration(testProps);

        fail(CONFIGURATION_EXCEPTION_MESSAGE);
    }

    @Test
    public void testApplyConfiguration() throws MessageClientException {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, testConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadConfigurationAndInit(eq(testConfiguration));
    }

    @Test
    public void testApplyEmptyConfiguration() throws MessageClientException {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, (ConnectorConfiguration) null);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector, never()).loadConfigurationAndInit(any(ConnectorConfiguration.class));
    }

    @Test(expected = ConfigurationException.class)
    public void testApplyConfigurationException() throws MessageClientException {
        Whitebox.setInternalState(testConfigHandler, CURRENT_CONFIGURATION_FIELD_NAME, testConfiguration);

        doThrow(new MessageClientException("")).when(mockedConnector)
                .loadConfigurationAndInit(any(ConnectorConfiguration.class));

        testConfigHandler.applyConfiguration();

        fail(CONFIGURATION_EXCEPTION_MESSAGE);
    }
}