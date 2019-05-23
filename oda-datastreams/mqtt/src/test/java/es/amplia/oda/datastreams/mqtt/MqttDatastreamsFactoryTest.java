package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttDatastreamsFactory.class)
public class MqttDatastreamsFactoryTest {

    private static final String TEST_READ_REQUEST_TOPIC = "test/event/topic";
    private static final String TEST_READ_RESPONSE_TOPIC = "test/event/topic";
    private static final String TEST_WRITE_REQUEST_TOPIC = "test/event/topic";
    private static final String TEST_WRITE_RESPONSE_TOPIC = "test/event/topic";
    private static final String TEST_EVENT_TOPIC = "test/event/topic";
    private static final String TEST_DATASTREAM_ID = "testDatastream";

    @Mock
    private MqttClient mockedClient;
    @Mock
    private  MqttDatastreamsPermissionManager mockedPermissionManager;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private EventDispatcher mockedEventDispatcher;

    private MqttDatastreamsFactory testFactory;

    @Mock
    private MqttDatastreamsGetter mockedGetter;
    @Mock
    private MqttDatastreamsSetter mockedSetter;
    @Mock
    private MqttDatastreamsEventHandler mockedEventHandler;

    @Before
    public void setUp() {
        testFactory = new MqttDatastreamsFactory(mockedClient, mockedPermissionManager, mockedSerializer,
                mockedEventDispatcher, TEST_READ_REQUEST_TOPIC, TEST_READ_RESPONSE_TOPIC, TEST_WRITE_REQUEST_TOPIC,
                TEST_WRITE_RESPONSE_TOPIC, TEST_EVENT_TOPIC);
    }

    @Test
    public void testCreateMqttDatastreamsEventHandler() throws Exception {
        PowerMockito.whenNew(MqttDatastreamsEventHandler.class).withAnyArguments().thenReturn(mockedEventHandler);

        testFactory.createDatastreamsEventHandler();

        PowerMockito.verifyNew(MqttDatastreamsEventHandler.class).withArguments(eq(mockedClient),
                eq(mockedPermissionManager), eq(mockedSerializer), eq(mockedEventDispatcher), eq(TEST_EVENT_TOPIC));
    }

    @Test
    public void testCreateMqttDatastreamGetter() throws Exception {
        PowerMockito.whenNew(MqttDatastreamsGetter.class).withAnyArguments().thenReturn(mockedGetter);

        testFactory.createDatastreamGetter(TEST_DATASTREAM_ID);

        PowerMockito.verifyNew(MqttDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM_ID), eq(mockedClient),
                eq(mockedPermissionManager), eq(mockedSerializer), eq(TEST_READ_RESPONSE_TOPIC),
                eq(TEST_READ_RESPONSE_TOPIC));
    }

    @Test
    public void testCreateMqttDatastreamSetter() throws Exception {
        PowerMockito.whenNew(MqttDatastreamsSetter.class).withAnyArguments().thenReturn(mockedSetter);

        testFactory.createDatastreamSetter(TEST_DATASTREAM_ID);

        PowerMockito.verifyNew(MqttDatastreamsSetter.class).withArguments(eq(TEST_DATASTREAM_ID), eq(mockedClient),
                eq(mockedPermissionManager), eq(mockedSerializer), eq(TEST_WRITE_REQUEST_TOPIC),
                eq(TEST_WRITE_RESPONSE_TOPIC));
    }
}