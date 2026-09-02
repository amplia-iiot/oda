package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttActionListener;
import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.OperationSender;
import es.amplia.oda.core.commons.interfaces.ResponseDispatcher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;

import es.amplia.oda.datastreams.mqtt.configuration.MqttDatastreamsConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MqttDatastreamsOrchestratorTest {

    private static final String TEST_SERVER_URI = "tcp://test.uri.com";
    private static final String TEST_CLIENT_ID = "test";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_EVENT_TOPIC = "test/event";
    private static final String TEST_REQUEST_TOPIC = "test/request";
    private static final String TEST_RESPONSE_TOPIC = "test/response";
    private static final int TEST_QOS = 1;
    private static final boolean TEST_RETAINED = false;
    private static final MqttDatastreamsConfiguration TEST_CONFIGURATION = new MqttDatastreamsConfiguration(TEST_SERVER_URI, TEST_CLIENT_ID, TEST_PASSWORD, TEST_EVENT_TOPIC,
                                                                                                            TEST_REQUEST_TOPIC, TEST_RESPONSE_TOPIC, TEST_QOS, TEST_RETAINED);
    private static final String MQTT_CLIENT_FIELD_NAME = "mqttClient";
    private static final String MQTT_DATASTREAMS_EVENT_FIELD_NAME = "mqttDatastreamsEvent";
    private static final String OP_SENDER_REG_FIELD_NAME = "mqttOperationSenderRegistration";

    private static final HashSet<String> TEST_EMPTY_SET = new HashSet<>();

    @Mock
    private MqttClientFactory mockedMqttClientFactory;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private EventPublisher mockedEventPublisher;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;

    private MqttDatastreamsOrchestrator testOrchestrator;

    @Mock
    private MqttClient mockedClient;
    @Mock
    private MqttDatastreamsEvent mockedEvent;
    @Mock
    private MqttOperationSender mockedOpSender;
    @Mock
    private ServiceRegistration<OperationSender> mockedOpSendRegistration;
    @Mock
    private ResponseDispatcher mockedResponseDispatcher;
    @Mock
    private BundleContext mockedContext;


    @Before
    public void setUp() {
        testOrchestrator = new MqttDatastreamsOrchestrator(mockedMqttClientFactory, mockedSerializer, mockedEventPublisher, mockedDeviceInfoProvider, mockedResponseDispatcher, mockedContext);
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        when(mockedMqttClientFactory.createMqttClient(anyString(), anyString())).thenReturn(mockedClient);
        List<List<?>> eventArgs = new ArrayList<>();
        List<List<?>> opSenderArgs = new ArrayList<>();
        try (MockedConstruction<MqttDatastreamsEvent> eventCons =
                     mockConstruction(MqttDatastreamsEvent.class,
                             (mock, mctx) -> eventArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MqttOperationSender> opSenderCons =
                     mockConstruction(MqttOperationSender.class,
                             (mock, mctx) -> opSenderArgs.add(new ArrayList<>(mctx.arguments())))) {

            testOrchestrator.loadConfiguration(TEST_CONFIGURATION);

            verify(mockedMqttClientFactory).createMqttClient(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID));
            assertEquals(1, eventCons.constructed().size());
            assertEquals(Arrays.asList(mockedEventPublisher, mockedClient, mockedSerializer, TEST_EVENT_TOPIC,
                    mockedDeviceInfoProvider, TEST_RESPONSE_TOPIC, mockedResponseDispatcher, TEST_EMPTY_SET),
                    eventArgs.get(0));
            assertEquals(1, opSenderCons.constructed().size());
            assertEquals(Arrays.asList(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS, TEST_RETAINED,
                    TEST_EMPTY_SET), opSenderArgs.get(0));
            Thread.sleep(100);
            verify(mockedClient).connect(eq(MqttConnectOptions.builder(TEST_CLIENT_ID, TEST_PASSWORD.toCharArray()).build()), any(MqttActionListener.class));
        }
    }

    @Test
    public void testLoadConfigurationWithOldConfiguration() throws Exception {
        Whitebox.setInternalState(testOrchestrator, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_EVENT_FIELD_NAME, mockedEvent);
        Whitebox.setInternalState(testOrchestrator, OP_SENDER_REG_FIELD_NAME, mockedOpSendRegistration);

        when(mockedMqttClientFactory.createMqttClient(anyString(), anyString())).thenReturn(mockedClient);
        List<List<?>> eventArgs = new ArrayList<>();
        List<List<?>> opSenderArgs = new ArrayList<>();
        try (MockedConstruction<MqttDatastreamsEvent> eventCons =
                     mockConstruction(MqttDatastreamsEvent.class,
                             (mock, mctx) -> eventArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<MqttOperationSender> opSenderCons =
                     mockConstruction(MqttOperationSender.class,
                             (mock, mctx) -> opSenderArgs.add(new ArrayList<>(mctx.arguments())))) {

            testOrchestrator.loadConfiguration(TEST_CONFIGURATION);

            verify(mockedEvent).unregisterFromEventSource();
            verify(mockedOpSendRegistration).unregister();
            verify(mockedClient).disconnect();
            verify(mockedMqttClientFactory).createMqttClient(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID));
            assertEquals(1, eventCons.constructed().size());
            assertEquals(Arrays.asList(mockedEventPublisher, mockedClient, mockedSerializer, TEST_EVENT_TOPIC,
                    mockedDeviceInfoProvider, TEST_RESPONSE_TOPIC, mockedResponseDispatcher, TEST_EMPTY_SET),
                    eventArgs.get(0));
            assertEquals(1, opSenderCons.constructed().size());
            assertEquals(Arrays.asList(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS, TEST_RETAINED,
                    TEST_EMPTY_SET), opSenderArgs.get(0));
            Thread.sleep(100);
            verify(mockedClient).connect(eq(MqttConnectOptions.builder(TEST_CLIENT_ID, TEST_PASSWORD.toCharArray()).build()), any(MqttActionListener.class));
        }
    }

    @Test
    public void testClose() throws MqttException {
        Whitebox.setInternalState(testOrchestrator, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_EVENT_FIELD_NAME, mockedEvent);
        Whitebox.setInternalState(testOrchestrator, OP_SENDER_REG_FIELD_NAME, mockedOpSendRegistration);

        testOrchestrator.close();

        verify(mockedEvent).unregisterFromEventSource();
        verify(mockedOpSendRegistration).unregister();
        verify(mockedClient).disconnect();
    }

    @Test
    public void testCloseMqttExceptionIsCaught() throws MqttException {
        Whitebox.setInternalState(testOrchestrator, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_EVENT_FIELD_NAME, mockedEvent);
        Whitebox.setInternalState(testOrchestrator, OP_SENDER_REG_FIELD_NAME, mockedOpSendRegistration);

        doThrow(new MqttException("", 0)).when(mockedClient).disconnect();

        testOrchestrator.close();

        verify(mockedEvent).unregisterFromEventSource();
        verify(mockedOpSendRegistration).unregister();
        verify(mockedClient).disconnect();
    }
}