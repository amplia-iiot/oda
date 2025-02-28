package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttCounters;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.operation.request.Operation;
import es.amplia.oda.core.commons.utils.operation.request.OperationRequest;
import es.amplia.oda.core.commons.utils.operation.request.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttCounters.class)
public class MqttOperationSenderTest {

    private static final String TEST_REQUEST_TOPIC = "test/request/topic";
    private static final int TEST_QOS_VALUE = 1;
    private static final boolean TEST_RETAINED_VALUE = false;
    private static final String TEST_DEVICEID_VALUE = "device_id";
    private static final String TEST_OPID_VALUE = "op_id";
    private static final String TEST_OPNAME_VALUE = "op_name";
    private static final String[] TEST_PATH1_VALUE = {"oda1"};
    private static final String[] TEST_PATH2_VALUE = {"oda1", "oda2"};
    private static final String[] TEST_PATH3_VALUE = {"oda7", "oda8", "oda9"};
    private static final byte[] TEST_PAYLOAD = { 0x1, 0x2, 0x3, 0x4 };
    private static final HashSet<String> TEST_ODALIST2_VALUE = new HashSet<String>(Arrays.asList("oda1", "oda2"));
    
    @Mock
    private MqttClient mockedClient;
    @Mock
    private Serializer mockedSerializer;
    
    private MqttOperationSender testHandler;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(MqttCounters.class);
    }

    @Test
    public void testConstructor() {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, null);
        assertEquals(TEST_REQUEST_TOPIC, Whitebox.getInternalState(testHandler, "requestTopic"));
    }

    @Test
    public void testDownlinkToPath() throws IOException {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, null);
        OperationRequest<Object> op = new OperationRequest<>(new Operation<>(new Request<Object>(TEST_OPID_VALUE, null, TEST_DEVICEID_VALUE, TEST_PATH2_VALUE, TEST_OPNAME_VALUE, null)));
        when(mockedSerializer.serialize(any())).thenReturn(TEST_PAYLOAD);
        
        testHandler.downlink(op);

        verify(mockedClient).publish(eq(TEST_REQUEST_TOPIC+"/oda2"), any(MqttMessage.class), eq(ContentType.JSON));
    }

    @Test
    public void testDownlinkToDevice() throws IOException {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, null);
        OperationRequest<Object> op = new OperationRequest<>(new Operation<>(new Request<Object>(TEST_OPID_VALUE, null, TEST_DEVICEID_VALUE, TEST_PATH1_VALUE, TEST_OPNAME_VALUE, null)));
        when(mockedSerializer.serialize(any())).thenReturn(TEST_PAYLOAD);
        
        testHandler.downlink(op);

        verify(mockedClient).publish(eq(TEST_REQUEST_TOPIC+"/device_id"), any(MqttMessage.class), eq(ContentType.JSON));
    }

    @Test
    public void testNextLevelOda() throws IOException {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, TEST_ODALIST2_VALUE);

        assertTrue(testHandler.isForNextLevel(TEST_PATH2_VALUE, TEST_DEVICEID_VALUE));
    }

    @Test
    public void testNextLevelDevice() throws IOException {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, TEST_ODALIST2_VALUE);

        assertTrue(testHandler.isForNextLevel(TEST_PATH3_VALUE, "oda1"));
    }

    @Test
    public void testNoNextLevelPathLength() throws IOException {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, TEST_ODALIST2_VALUE);

        assertFalse(testHandler.isForNextLevel(TEST_PATH1_VALUE, TEST_DEVICEID_VALUE));
    }

    @Test
    public void testNoNextLevel() throws IOException {
        testHandler = new MqttOperationSender(mockedClient, mockedSerializer, TEST_REQUEST_TOPIC, TEST_QOS_VALUE, TEST_RETAINED_VALUE, TEST_ODALIST2_VALUE);

        assertFalse(testHandler.isForNextLevel(TEST_PATH3_VALUE, TEST_DEVICEID_VALUE));
    }

}