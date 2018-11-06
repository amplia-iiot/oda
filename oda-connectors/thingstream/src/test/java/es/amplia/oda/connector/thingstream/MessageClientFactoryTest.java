package es.amplia.oda.connector.thingstream;

import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration;
import es.amplia.oda.connector.thingstream.mqttsn.USSDModemImplementation;

import com.myriadgroup.iot.sdk.client.impl.mqtt.MQTTPahoMessageClientImpl;
import com.myriadgroup.iot.sdk.client.impl.mqttsn.MQTTSNModemMessageClientImpl;
import com.myriadgroup.iot.sdk.client.impl.ussd.USSDModemProtocolLayerImpl;
import com.myriadgroup.iot.sdk.client.message.IMessageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MessageClientFactory.class)
public class MessageClientFactoryTest {

    private static final String TOPIC = "device/topic/test";
    private static final ConnectorConfiguration.QOS QOS = ConnectorConfiguration.QOS.QOS_0;
    private static final String CLIENT_ID = "testClientId";
    private static final int SHORT_CODE = 123;
    private static final String MQTT_HOST = "http://testhost.com";
    private static final int MQTT_PORT = 1883;
    private static final String MQTT_CLIENT_ID = "testMqttClientId";
    private static final String MQTT_USERNAME = "testUsername";
    private static final String MQTT_PASSWORD = "testPassword";

    @Mock
    private ATManager mockedAtManager;
    @Mock
    private USSDModemImplementation mockedModemImpl;
    @Mock
    private USSDModemProtocolLayerImpl mockedProtocolLayerImpl;
    @Mock
    private MQTTSNModemMessageClientImpl mockedModemMessageClient;
    @Mock
    private MQTTPahoMessageClientImpl mockedMqttClientImpl;

    @Test
    public void testCreateMessageClientMqttSn() throws Exception {
        ConnectorConfiguration mqttSnConfiguration =
                ConnectorConfiguration.builder()
                        .clientType(ConnectorConfiguration.ClientType.MQTTSN)
                        .dataTopic(TOPIC)
                        .qos(QOS)
                        .clientId(CLIENT_ID)
                        .shortCode(SHORT_CODE)
                        .build();

        PowerMockito.whenNew(USSDModemImplementation.class).withAnyArguments().thenReturn(mockedModemImpl);
        PowerMockito.whenNew(USSDModemProtocolLayerImpl.class).withAnyArguments().thenReturn(mockedProtocolLayerImpl);
        PowerMockito.whenNew(MQTTSNModemMessageClientImpl.class).withAnyArguments()
                .thenReturn(mockedModemMessageClient);

        IMessageClient messageClient = MessageClientFactory.createMessageClient(mqttSnConfiguration, mockedAtManager);

        assertNotNull(messageClient);
        PowerMockito.verifyNew(USSDModemImplementation.class).withArguments(eq(mockedAtManager));
        PowerMockito.verifyNew(USSDModemProtocolLayerImpl.class).withArguments(eq(mockedModemImpl), eq(SHORT_CODE));
        PowerMockito.verifyNew(MQTTSNModemMessageClientImpl.class)
                .withArguments(eq(CLIENT_ID), eq(mockedProtocolLayerImpl));
    }

    @Test
    public void testCreateMessageClientMqtt() throws Exception {
        ConnectorConfiguration mqttSnConfiguration =
                ConnectorConfiguration.builder()
                        .clientType(ConnectorConfiguration.ClientType.MQTT)
                        .dataTopic(TOPIC)
                        .qos(QOS)
                        .mqttHost(MQTT_HOST)
                        .mqttClientId(MQTT_CLIENT_ID)
                        .mqttUsername(MQTT_USERNAME)
                        .mqttPassword(MQTT_PASSWORD)
                        .build();

        PowerMockito.whenNew(MQTTPahoMessageClientImpl.class).withAnyArguments().thenReturn(mockedMqttClientImpl);

        IMessageClient messageClient = MessageClientFactory.createMessageClient(mqttSnConfiguration, mockedAtManager);

        assertNotNull(messageClient);
        PowerMockito.verifyNew(MQTTPahoMessageClientImpl.class).withArguments(eq(MQTT_HOST), eq(MQTT_PORT),
                eq(MQTT_CLIENT_ID), eq(MQTT_USERNAME), eq(MQTT_PASSWORD));
    }
}