package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerWithKey;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttDatastreamsOrchestrator.class)
public class MqttDatastreamsOrchestratorTest {

    private static final String TEST_SERVER_URI = "tcp://test.uri.com";
    private static final String TEST_CLIENT_ID = "test";
    private static final String TEST_EVENT_TOPIC = "test/event";
    private static final String TEST_ENABLE_TOPIC = "test/enable";
    private static final String TEST_DISABLE_TOPIC = "test/disable";
    private static final String TEST_READ_REQUEST_TOPIC = "test/read/request";
    private static final String TEST_READ_RESPONSE_TOPIC = "test/read/response";
    private static final String TEST_WRITE_REQUEST_TOPIC = "test/write/request";
    private static final String TEST_WRITE_RESPONSE_TOPIC = "test/write/response";
    private static final String TEST_LWT_TOPIC = "test/lwt";
    private static final String MQTT_CLIENT_FIELD_NAME = "mqttClient";
    private static final String MQTT_DATASTREAMS_MANAGER_FIELD_NAME = "mqttDatastreamsManager";
    private static final String MQTT_DATASTREAMS_EVENT_HANDLER_FIELD_NAME = "mqttDatastreamsEventHandler";
    private static final String MQTT_DATASTREAMS_DISCOVERY_HANDLER_FIELD_NAME = "mqttDatastreamDiscoveryHandler";
    private static final String MQTT_DATASTREAMS_LWT_HANDLER_FIELD_NAME = "mqttDatastreamsLwtHandler";
    private static final MqttDatastreamsConfiguration TEST_CONFIGURATION = new MqttDatastreamsConfiguration(TEST_SERVER_URI, TEST_CLIENT_ID, TEST_ENABLE_TOPIC, TEST_DISABLE_TOPIC,
            TEST_EVENT_TOPIC, TEST_READ_REQUEST_TOPIC, TEST_READ_RESPONSE_TOPIC, TEST_WRITE_REQUEST_TOPIC,
            TEST_WRITE_RESPONSE_TOPIC, TEST_LWT_TOPIC);
    private static final List<DatastreamInfoWithPermission> TEST_INITIAL_DATASTREAM_CONF = new ArrayList<>();


    @Mock
    private MqttClientFactory mockedMqttClientFactory;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private EventDispatcher mockedEventDispatcher;
    @Mock
    private ServiceRegistrationManagerWithKey<String, DatastreamsGetter> mockedDatastreamsGetterRegistrationManager;
    @Mock
    private ServiceRegistrationManagerWithKey<String, DatastreamsSetter> mockedDatastreamsSetterRegistrationManager;

    private MqttDatastreamsOrchestrator testOrchestrator;

    @Mock
    private MqttClient mockedClient;
    @Mock
    private MqttDatastreamsPermissionManager mockedPermissionManager;
    @Mock
    private MqttDatastreamsFactory mockedDatastreamsFactory;
    @Mock
    private MqttDatastreamsManager mockedDatastreamsManager;
    @Mock
    private MqttDatastreamsEventHandler mockedEventHandler;
    @Mock
    private MqttDatastreamDiscoveryHandler mockedDatastreamsDiscoveryHandler;
    @Mock
    private MqttDatastreamsLwtHandler mockedDatastreamsLwtHandler;


    @Before
    public void setUp() {
        testOrchestrator = new MqttDatastreamsOrchestrator(mockedMqttClientFactory, mockedSerializer,
                mockedEventDispatcher, mockedDatastreamsGetterRegistrationManager,
                mockedDatastreamsSetterRegistrationManager);
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        when(mockedMqttClientFactory.createMqttClient(anyString(), anyString())).thenReturn(mockedClient);
        PowerMockito.whenNew(MqttDatastreamsPermissionManager.class).withAnyArguments()
                .thenReturn(mockedPermissionManager);
        PowerMockito.whenNew(MqttDatastreamsFactory.class).withAnyArguments().thenReturn(mockedDatastreamsFactory);
        PowerMockito.whenNew(MqttDatastreamsManager.class).withAnyArguments().thenReturn(mockedDatastreamsManager);
        PowerMockito.whenNew(MqttDatastreamDiscoveryHandler.class).withAnyArguments()
                .thenReturn(mockedDatastreamsDiscoveryHandler);
        PowerMockito.whenNew(MqttDatastreamsLwtHandler.class).withAnyArguments().thenReturn(mockedDatastreamsLwtHandler);

        testOrchestrator.loadConfiguration(TEST_CONFIGURATION, TEST_INITIAL_DATASTREAM_CONF);

        verify(mockedMqttClientFactory).createMqttClient(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID));
        verify(mockedClient).connect();
        PowerMockito.verifyNew(MqttDatastreamsPermissionManager.class).withNoArguments();
        PowerMockito.verifyNew(MqttDatastreamsFactory.class).withArguments(eq(mockedClient),
                eq(mockedPermissionManager), eq(mockedSerializer), eq(mockedEventDispatcher), eq(TEST_READ_REQUEST_TOPIC),
                eq(TEST_READ_RESPONSE_TOPIC), eq(TEST_WRITE_REQUEST_TOPIC), eq(TEST_WRITE_RESPONSE_TOPIC), eq(TEST_EVENT_TOPIC));
        verify(mockedDatastreamsFactory).createDatastreamsEventHandler();
        PowerMockito.verifyNew(MqttDatastreamsManager.class).withArguments(eq(mockedDatastreamsGetterRegistrationManager),
                eq(mockedDatastreamsSetterRegistrationManager), eq(mockedDatastreamsFactory));
        PowerMockito.verifyNew(MqttDatastreamDiscoveryHandler.class).withArguments(eq(mockedClient), eq(mockedSerializer),
                eq(mockedDatastreamsManager), eq(mockedPermissionManager), eq(TEST_ENABLE_TOPIC), eq(TEST_DISABLE_TOPIC));
        verify(mockedDatastreamsDiscoveryHandler).init(TEST_INITIAL_DATASTREAM_CONF);
        PowerMockito.verifyNew(MqttDatastreamsLwtHandler.class).withArguments(eq(mockedClient), eq(mockedSerializer),
                eq(mockedPermissionManager), eq(mockedDatastreamsManager), eq(TEST_LWT_TOPIC));
    }

    @Test
    public void testLoadConfigurationWithOldConfiguration() throws Exception {
        Whitebox.setInternalState(testOrchestrator, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_MANAGER_FIELD_NAME, mockedDatastreamsManager);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_EVENT_HANDLER_FIELD_NAME, mockedEventHandler);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_DISCOVERY_HANDLER_FIELD_NAME, mockedDatastreamsDiscoveryHandler);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_LWT_HANDLER_FIELD_NAME, mockedDatastreamsLwtHandler);

        when(mockedMqttClientFactory.createMqttClient(anyString(), anyString())).thenReturn(mockedClient);
        PowerMockito.whenNew(MqttDatastreamsPermissionManager.class).withAnyArguments()
                .thenReturn(mockedPermissionManager);
        PowerMockito.whenNew(MqttDatastreamsFactory.class).withAnyArguments().thenReturn(mockedDatastreamsFactory);
        PowerMockito.whenNew(MqttDatastreamsManager.class).withAnyArguments().thenReturn(mockedDatastreamsManager);
        PowerMockito.whenNew(MqttDatastreamDiscoveryHandler.class).withAnyArguments()
                .thenReturn(mockedDatastreamsDiscoveryHandler);
        PowerMockito.whenNew(MqttDatastreamsLwtHandler.class).withAnyArguments().thenReturn(mockedDatastreamsLwtHandler);

        testOrchestrator.loadConfiguration(TEST_CONFIGURATION, TEST_INITIAL_DATASTREAM_CONF);

        verify(mockedDatastreamsLwtHandler).close();
        verify(mockedDatastreamsDiscoveryHandler).close();
        verify(mockedEventHandler).close();
        verify(mockedDatastreamsManager).close();
        verify(mockedClient).disconnect();
        verify(mockedMqttClientFactory).createMqttClient(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID));
        verify(mockedClient).connect();
        PowerMockito.verifyNew(MqttDatastreamsPermissionManager.class).withNoArguments();
        PowerMockito.verifyNew(MqttDatastreamsFactory.class).withArguments(eq(mockedClient),
                eq(mockedPermissionManager), eq(mockedSerializer), eq(mockedEventDispatcher), eq(TEST_READ_REQUEST_TOPIC),
                eq(TEST_READ_RESPONSE_TOPIC), eq(TEST_WRITE_REQUEST_TOPIC), eq(TEST_WRITE_RESPONSE_TOPIC),
                eq(TEST_EVENT_TOPIC));
        verify(mockedDatastreamsFactory).createDatastreamsEventHandler();
        PowerMockito.verifyNew(MqttDatastreamsManager.class).withArguments(eq(mockedDatastreamsGetterRegistrationManager),
                eq(mockedDatastreamsSetterRegistrationManager), eq(mockedDatastreamsFactory));
        PowerMockito.verifyNew(MqttDatastreamDiscoveryHandler.class).withArguments(eq(mockedClient), eq(mockedSerializer),
                eq(mockedDatastreamsManager), eq(mockedPermissionManager), eq(TEST_ENABLE_TOPIC), eq(TEST_DISABLE_TOPIC));
        verify(mockedDatastreamsDiscoveryHandler).init(TEST_INITIAL_DATASTREAM_CONF);
        PowerMockito.verifyNew(MqttDatastreamsLwtHandler.class).withArguments(eq(mockedClient), eq(mockedSerializer),
                eq(mockedPermissionManager), eq(mockedDatastreamsManager), eq(TEST_LWT_TOPIC));
    }

    @Test
    public void testClose() throws MqttException {
        Whitebox.setInternalState(testOrchestrator, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_MANAGER_FIELD_NAME, mockedDatastreamsManager);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_EVENT_HANDLER_FIELD_NAME, mockedEventHandler);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_DISCOVERY_HANDLER_FIELD_NAME, mockedDatastreamsDiscoveryHandler);

        testOrchestrator.close();

        verify(mockedDatastreamsDiscoveryHandler).close();
        verify(mockedEventHandler).close();
        verify(mockedDatastreamsManager).close();
        verify(mockedClient).disconnect();
    }

    @Test
    public void testCloseMqttExceptionIsCaught() throws MqttException {
        Whitebox.setInternalState(testOrchestrator, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_MANAGER_FIELD_NAME, mockedDatastreamsManager);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_EVENT_HANDLER_FIELD_NAME, mockedEventHandler);
        Whitebox.setInternalState(testOrchestrator, MQTT_DATASTREAMS_DISCOVERY_HANDLER_FIELD_NAME, mockedDatastreamsDiscoveryHandler);

        doThrow(new MqttException("")).when(mockedClient).disconnect();

        testOrchestrator.close();

        verify(mockedDatastreamsDiscoveryHandler).close();
        verify(mockedEventHandler).close();
        verify(mockedDatastreamsManager).close();
        verify(mockedClient).disconnect();
    }
}