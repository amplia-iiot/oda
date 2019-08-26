package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.Input;
import es.amplia.oda.dispatcher.opengate.domain.InputOperation;
import es.amplia.oda.dispatcher.opengate.domain.Parameter;
import es.amplia.oda.dispatcher.opengate.domain.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private static final byte[] TEST_PAYLOAD = { 0x1, 0x2, 0x3, 0x4 };
    private static final CompletableFuture<byte[]> EXPECTED_FUTURE = CompletableFuture.completedFuture(TEST_PAYLOAD);

    @Mock
    private Serializer mockedSerializer;
    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private OperationProcessor mockedOperationProcessor;
    @InjectMocks
    private OpenGateOperationDispatcher testDispatcher;

    @Test
    public void testProcess() throws IOException {
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(TEST_INPUT);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(EXPECTED_FUTURE);

        CompletableFuture<byte[]> result = testDispatcher.process(TEST_PAYLOAD);

        assertEquals(EXPECTED_FUTURE, result);
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(TEST_DEVICE_ID), eq(TEST_DEVICE_ID), eq(TEST_REQUEST));
    }

    @Test
    public void testProcessNullDeviceIdInRequest() throws IOException {
        Request request = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, null, TEST_PATH, TEST_OPERATION_NAME,
                TEST_PARAMETERS);
        InputOperation operation = new InputOperation(request);
        Input input = new Input(operation);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(EXPECTED_FUTURE);

        CompletableFuture<byte[]> result = testDispatcher.process(TEST_PAYLOAD);

        assertEquals(EXPECTED_FUTURE, result);
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request));
    }

    @Test
    public void testProcessEmptyDeviceIdInRequest() throws IOException {
        Request request = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, "", TEST_PATH, TEST_OPERATION_NAME,
                TEST_PARAMETERS);
        InputOperation operation = new InputOperation(request);
        Input input = new Input(operation);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(EXPECTED_FUTURE);

        CompletableFuture<byte[]> result = testDispatcher.process(TEST_PAYLOAD);

        assertEquals(EXPECTED_FUTURE, result);
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request));
    }

    @Test
    public void testProcessSameDeviceIdAInRequestAndHostDeviceId() throws IOException {
        Request request = new Request(TEST_REQUEST_ID, TEST_TIMESTAMP, TEST_HOST_DEVICE_ID, TEST_PATH,
                TEST_OPERATION_NAME, TEST_PARAMETERS);
        InputOperation operation = new InputOperation(request);
        Input input = new Input(operation);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(input);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_HOST_DEVICE_ID);
        when(mockedOperationProcessor.process(anyString(), anyString(), any(Request.class)))
                .thenReturn(EXPECTED_FUTURE);

        CompletableFuture<byte[]> result = testDispatcher.process(TEST_PAYLOAD);

        assertEquals(EXPECTED_FUTURE, result);
        verify(mockedSerializer).deserialize(eq(TEST_PAYLOAD), eq(Input.class));
        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedOperationProcessor).process(eq(""), eq(TEST_HOST_DEVICE_ID), eq(request));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessNullInput() {
        testDispatcher.process(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeException() throws IOException {
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        testDispatcher.process(TEST_PAYLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeNullObject() throws IOException {
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(null);

        testDispatcher.process(TEST_PAYLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeNullOperation() throws IOException {
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(new Input());

        testDispatcher.process(TEST_PAYLOAD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProcessDeserializeNullRequest() throws IOException {
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(new Input(new InputOperation()));

        testDispatcher.process(TEST_PAYLOAD);
    }
}