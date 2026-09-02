package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.custom.InputOperationCustomOperation;
import es.amplia.oda.dispatcher.opengate.domain.custom.RequestCustomOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.InputGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.InputOperationGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.ParameterGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.RequestGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OpenGateOperationDispatcherTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final ParameterGeneralOperation TEST_PARAMETER = new ParameterGeneralOperation();
    private static final RequestGeneralOperation TEST_REQUEST = new RequestGeneralOperation(TEST_PARAMETER);
    private static final InputOperationGeneralOperation TEST_OPERATION = new InputOperationGeneralOperation(TEST_REQUEST);
    private static final InputGeneralOperation TEST_INPUT = new InputGeneralOperation(TEST_OPERATION);
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
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class), anyInt()))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(3)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(InputGeneralOperation.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(TEST_REQUEST), eq(80));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessDefaultContentType() throws IOException, ExecutionException, InterruptedException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(TEST_INPUT);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class), anyInt()))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(3)).getSerializer(eq(ContentType.JSON));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(InputGeneralOperation.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(TEST_REQUEST), eq(80));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessNullDeviceIdInRequest() throws IOException, ExecutionException, InterruptedException {
        RequestGeneralOperation request = new RequestGeneralOperation(TEST_PARAMETER);
        InputOperationGeneralOperation operation = new InputOperationGeneralOperation(request);
        InputGeneralOperation input = new InputGeneralOperation(operation);

        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class), anyInt()))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(3)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(InputGeneralOperation.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request), eq(80));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessEmptyDeviceIdInRequest() throws IOException, ExecutionException, InterruptedException {
        RequestGeneralOperation request = new RequestGeneralOperation(TEST_PARAMETER);
        InputOperationGeneralOperation operation = new InputOperationGeneralOperation(request);
        InputGeneralOperation input = new InputGeneralOperation(operation);

        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class), anyInt()))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(3)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(InputGeneralOperation.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request), eq(80));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessSameDeviceIdAInRequestAndHostDeviceId() throws IOException, ExecutionException, InterruptedException {
        RequestGeneralOperation request = new RequestGeneralOperation(TEST_PARAMETER);
        InputOperationGeneralOperation operation = new InputOperationGeneralOperation(request);
        InputGeneralOperation input = new InputGeneralOperation(operation);

        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class), anyInt()))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenReturn(TEST_PAYLOAD_2);

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        byte[] result = future.get();

        assertEquals(TEST_PAYLOAD_2, result);
        verify(mockedSerializerProvider,times(3)).getSerializer(eq(TEST_CONTENT_TYPE));
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(InputGeneralOperation.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request), eq(80));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessNullInput() {
        assertThrows(IllegalArgumentException.class, () -> testDispatcher.process(null));
    }

    @Test
    public void testProcessDeserializeException() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        assertThrows(IllegalArgumentException.class, () -> testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE));
    }

    @Test
    public void testProcessDeserializeNullObject() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE));
    }

    public void testProcessDeserializeNullOperation() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(new InputGeneralOperation());

        assertNull(testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE));
    }

    public void testProcessDeserializeNullRequest() throws IOException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(new InputGeneralOperation(new InputOperationGeneralOperation()));

        assertNull(testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE));
    }

    @Test
    public void testProcessSerializeOutputException() throws IOException, ExecutionException, InterruptedException {
        when(mockedSerializerProvider.getSerializer(any(ContentType.class))).thenReturn(mockedSerializer);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(TEST_INPUT);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class), anyInt()))
                .thenReturn(FUTURE_OUTPUT);
        when(mockedSerializer.serialize(any(Output.class))).thenThrow(new IOException());

        CompletableFuture<byte[]> future = testDispatcher.process(TEST_PAYLOAD, TEST_CONTENT_TYPE);
        assertThrows(ExecutionException.class, () -> future.get());
    }
}