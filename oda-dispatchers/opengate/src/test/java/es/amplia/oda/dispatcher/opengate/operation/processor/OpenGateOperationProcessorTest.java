package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.dispatcher.opengate.domain.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class OpenGateOperationProcessorTest {

    private static final String TEST_OPERATION = "TEST_OPERATION";
    private static final String TEST_UNKNOWN_OPERATION = "UNKNOWN_OPERATION";
    private static final String TEST_DEVICE_FOR_OPERATIONS = "device1";
    private static final String TEST_DEVICE_FOR_RESPONSE = "device2";


    @Mock
    private OperationProcessor mockedOperationProcessor;
    @Mock
    private OperationProcessor mockedCustomOperationProcessor;

    private OpenGateOperationProcessor testProcessor;

    @Before
    public void setUp() {
        Map<String, OperationProcessor> mockedOperationProcessors =
                Collections.singletonMap(TEST_OPERATION, mockedOperationProcessor);

        testProcessor = new OpenGateOperationProcessor(mockedOperationProcessors, mockedCustomOperationProcessor);
    }

    @Test
    public void testProcess() {
        Request testRequest = new Request();
        testRequest.setName(TEST_OPERATION);

        testProcessor.process(TEST_DEVICE_FOR_OPERATIONS, TEST_DEVICE_FOR_RESPONSE, testRequest);

        verify(mockedOperationProcessor)
                .process(eq(TEST_DEVICE_FOR_OPERATIONS), eq(TEST_DEVICE_FOR_RESPONSE), eq(testRequest));
        verifyZeroInteractions(mockedCustomOperationProcessor);
    }

    @Test
    public void testProcessCustomOperation() {
        Request testRequest = new Request();
        testRequest.setName(TEST_UNKNOWN_OPERATION);

        testProcessor.process(TEST_DEVICE_FOR_OPERATIONS, TEST_DEVICE_FOR_RESPONSE, testRequest);

        verify(mockedCustomOperationProcessor)
                .process(eq(TEST_DEVICE_FOR_OPERATIONS), eq(TEST_DEVICE_FOR_RESPONSE), eq(testRequest));
        verifyZeroInteractions(mockedOperationProcessor);
    }
}