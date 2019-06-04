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

@RunWith(MockitoJUnitRunner.class)
public class OpenGateOperationProcessorTest {

    private static final String TEST_OPERATION = "TEST_OPERATION";
    private static final String TEST_UNSUPPORTED = "TEST_UNSUPPORTED";
    private static final String TEST_DEVICE_FOR_OPERATIONS = "device1";
    private static final String TEST_DEVICE_FOR_RESPONSE = "device2";


    @Mock
    private OperationProcessor mockedOperationProcessor;
    @Mock
    private OperationProcessor mockedUnsupportedProcessor;

    private OpenGateOperationProcessor testProcessor;

    @Before
    public void setUp() {
        Map<String, OperationProcessor> mockedOperationProcessors =
                Collections.singletonMap(TEST_OPERATION, mockedOperationProcessor);

        testProcessor = new OpenGateOperationProcessor(mockedOperationProcessors, mockedUnsupportedProcessor);
    }

    @Test
    public void testProcess() {
        Request testRequest = new Request();
        testRequest.setName(TEST_OPERATION);

        testProcessor.process(TEST_DEVICE_FOR_OPERATIONS, TEST_DEVICE_FOR_RESPONSE, testRequest);

        verify(mockedOperationProcessor)
                .process(eq(TEST_DEVICE_FOR_OPERATIONS), eq(TEST_DEVICE_FOR_RESPONSE), eq(testRequest));
    }

    @Test
    public void testProcessUnsupported() {
        Request testRequest = new Request();
        testRequest.setName(TEST_UNSUPPORTED);

        testProcessor.process(TEST_DEVICE_FOR_OPERATIONS, TEST_DEVICE_FOR_RESPONSE, testRequest);

        verify(mockedUnsupportedProcessor)
                .process(eq(TEST_DEVICE_FOR_OPERATIONS), eq(TEST_DEVICE_FOR_RESPONSE), eq(testRequest));
    }
}