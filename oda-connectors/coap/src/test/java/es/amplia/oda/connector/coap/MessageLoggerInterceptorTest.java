package es.amplia.oda.connector.coap;

import org.eclipse.californium.core.coap.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import static es.amplia.oda.connector.coap.MessageLoggerInterceptor.*;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MessageLoggerInterceptor.class, LoggerFactory.class })
public class MessageLoggerInterceptorTest {

    private static final InetAddress TEST_ADDRESS = InetAddress.getLoopbackAddress();
    private static final int TEST_PORT = 9999;
    private static final Request TEST_REQUEST =
            (Request) Request.newPost().setDestination(TEST_ADDRESS).setDestinationPort(TEST_PORT);
    static {
        TEST_REQUEST.setSource(TEST_ADDRESS);
        TEST_REQUEST.setSourcePort(TEST_PORT);
    }
    private static final Response TEST_RESPONSE =
            Response.createResponse(TEST_REQUEST, CoAP.ResponseCode.CREATED);
    static {
        TEST_RESPONSE.setSource(TEST_ADDRESS);
        TEST_RESPONSE.setSourcePort(TEST_PORT);
    }
    private static final EmptyMessage TEST_EMPTY_MESSAGE = EmptyMessage.newACK(TEST_REQUEST);
    static {
        TEST_EMPTY_MESSAGE.setSource(TEST_ADDRESS);
        TEST_EMPTY_MESSAGE.setSourcePort(TEST_PORT);
    }


    private final MessageLoggerInterceptor testLoggerInterceptor = new MessageLoggerInterceptor();
    @Mock
    private Logger mockedLogger;


    @Before
    public void setUp() {
        Whitebox.setInternalState(MessageLoggerInterceptor.class, "LOGGER", mockedLogger);
    }

    @Test
    public void testSendRequest() {
        testLoggerInterceptor.sendRequest(TEST_REQUEST);

        verify(mockedLogger).info(anyString(), eq(REQUEST_MESSAGE), eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_REQUEST));
    }

    @Test
    public void testSendResponse() {
        testLoggerInterceptor.sendResponse(TEST_RESPONSE);

        verify(mockedLogger).info(anyString(), eq(RESPONSE_MESSAGE), eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_RESPONSE));
    }

    @Test
    public void testSendEmptyMessage() {
        testLoggerInterceptor.sendEmptyMessage(TEST_EMPTY_MESSAGE);

        verify(mockedLogger).info(anyString(), eq(EMPTY_MESSAGE), eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_EMPTY_MESSAGE));
    }

    @Test
    public void testReceiveRequest() {
        testLoggerInterceptor.receiveRequest(TEST_REQUEST);

        verify(mockedLogger).info(anyString(), eq(REQUEST_MESSAGE), eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_REQUEST));
    }

    @Test
    public void testReceiveResponse() {
        testLoggerInterceptor.receiveResponse(TEST_RESPONSE);

        verify(mockedLogger).info(anyString(), eq(RESPONSE_MESSAGE), eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_RESPONSE));

    }

    @Test
    public void testReceiveEmptyMessage() {
        testLoggerInterceptor.receiveEmptyMessage(TEST_EMPTY_MESSAGE);

        verify(mockedLogger).info(anyString(), eq(EMPTY_MESSAGE), eq(TEST_ADDRESS), eq(TEST_PORT), eq(TEST_EMPTY_MESSAGE));
    }
}