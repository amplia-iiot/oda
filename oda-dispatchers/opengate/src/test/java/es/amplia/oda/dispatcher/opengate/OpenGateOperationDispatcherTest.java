package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.dispatcher.opengate.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenGateOperationDispatcherTest {

    private static final String TEST_REQUEST_ID = "testRequest";
    private static final Long TEST_TIMESTAMP = 123456789L;
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] { "gateway1", "gateway2" };
    private static final String TEST_OPERATION_NAME = "TEST_OPERATION";
    private static final List<Parameter> TEST_PARAMETERS = Collections.emptyList();
    private static final Request TEST_REQUEST = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, TEST_DEVICE_ID, TEST_PATH,
            TEST_OPERATION_NAME, TEST_PARAMETERS);
    private static final InputOperation TEST_OPERATION = new InputOperation(TEST_REQUEST);
    private static final Input TEST_INPUT = new Input(TEST_OPERATION);
    private static final String TEST_HOST_DEVICE_ID = "hostDeviceId";
    private static final Output TEST_OUTPUT = new Output("", null);
    private static final CompletableFuture<Output> FUTURE_OUTPUT = CompletableFuture.completedFuture(TEST_OUTPUT);
    private static final byte[] TEST_PAYLOAD = { 0x1, 0x2, 0x3, 0x4 };
    private static final ContentType TEST_CONTENT_TYPE = ContentType.CBOR;
    private static final byte[] TEST_PAYLOAD_2 = { 0x5, 0x6, 0x7, 0x8 };


    @Mock
    private SerializerProvider mockedSerializerProvider;
    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private OperationProcessor mockedOperationProcessor;
    @InjectMocks
    private OpenGateOperationDispatcher testDispatcher;

    @Mock
    private Serializer mockedSerializer;

    @Test
    public void testProcess() throws IOException, ExecutionException, InterruptedException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(TEST_INPUT);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(2)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(TEST_DEVICE_ID), eq(TEST_DEVICE_ID), eq(TEST_REQUEST));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessDefaultContentType() throws IOException, ExecutionException, InterruptedException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(TEST_INPUT);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(2)).getSerializer(eq(ContentType.JSON));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(TEST_DEVICE_ID), eq(TEST_DEVICE_ID), eq(TEST_REQUEST));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessNullDeviceIdInRequest() throws IOException, ExecutionException, InterruptedException {
        Request request = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, null, TEST_PATH, TEST_OPERATION_NAME,
                TEST_PARAMETERS);
        InputOperation operation = new InputOperation(request);
        Input input = new Input(operation);

        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(2)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessEmptyDeviceIdInRequest() throws IOException, ExecutionException, InterruptedException {
        Request request = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, "", TEST_PATH, TEST_OPERATION_NAME,
                TEST_PARAMETERS);
        InputOperation operation = new InputOperation(request);
        Input input = new Input(operation);

        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(2)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessSameDeviceIdAInRequestAndHostDeviceId() throws IOException, ExecutionException, InterruptedException {
        Request request = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, TEST_HOST_DEVICE_ID, TEST_PATH,
                TEST_OPERATION_NAME, TEST_PARAMETERS);
        InputOperation operation = new InputOperation(request);
        Input input = new Input(operation);

        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(2)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessNullInput() {
        testDispatcher.process(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeException() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeNullObject() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(null);

        testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeNullOperation() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(new Input());

        testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeNullRequest() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(new Input(new InputOperation()));

        testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
    }

    @Test(expected = ExecutionException.class)
    public void testProcessSerializeOutputException() throws IOException, ExecutionException, InterruptedException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(TEST_INPUT);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenThrow(new IOException());

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        future.get();
    }
}