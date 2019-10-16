package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.ATManagerImpl;
import es.amplia.oda.hardware.atmanager.ATParser;
import es.amplia.oda.hardware.atmanager.api.ATCommand;
import es.amplia.oda.hardware.atmanager.api.ATEvent;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atmanager.api.ATResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ATManagerImplTest {

    private static final String AN_EVENT = "+CUSD";
    private static final String AN_EVENT_STRING = AN_EVENT + ": 1,2\r";
    private static final String[] AN_EVENT_PARAMETERS = {"1", "2"};

    private static final String A_COMMAND = "+RDCURR";
    private static final String A_COMMAND_STRING = "AT" + A_COMMAND + "=1,\"baz\"\r";
    private static final String[] A_COMMAND_PARAMETERS = {"1", "baz"};
    private static final ATCommand A_COMMAND_AS_CLASS = ATCommand.extendedSetCommand(A_COMMAND, A_COMMAND_PARAMETERS);

    private static final String ANOTHER_COMMAND = "+FOO";
    private static final String ANOTHER_COMMAND_STRING = "AT" + ANOTHER_COMMAND + "=1,2";
    private static final String[] ANOTHER_COMMAND_PARAMETERS = {"1", "2"};
    private static final ATCommand ANOTHER_COMMAND_AS_CLASS = ATCommand.extendedSetCommand(ANOTHER_COMMAND, ANOTHER_COMMAND_PARAMETERS);
    private static final long TIMEOUT = 2;
    private static final TimeUnit unit = TimeUnit.SECONDS;
    private static final String PEER_RESPONSE = "PEER_RESPONSE_1";

    @Mock
    private Consumer<ATEvent> eventHandler;
    @Mock
    private Function<ATCommand, ATResponse> aCommandHandler;
    @Mock
    private Function<ATCommand, ATResponse> anotherCommandHandler;
    @Mock
    private ATParser atParser;
    @Mock
    private OutputStream outputStream;
    private ATManager atManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        atManager = new ATManagerImpl(atParser, outputStream);
        when(atParser.process(AN_EVENT_STRING)).thenReturn(ATParser.Result.unsolicitedResponse(AN_EVENT, AN_EVENT_PARAMETERS));

        when(atParser.process(A_COMMAND_STRING)).thenReturn(ATParser.Result.commands(Collections.singletonList(A_COMMAND_AS_CLASS)));
        when(aCommandHandler.apply(A_COMMAND_AS_CLASS)).thenReturn(ATResponse.ok());

        when(atParser.process(ANOTHER_COMMAND_STRING)).thenReturn(ATParser.Result.commands(Collections.singletonList(ANOTHER_COMMAND_AS_CLASS)));
    }

    @SuppressWarnings("SameReturnValue")
    private void peerWillRespondWith(ATParser.Result... responses) throws IOException {
        doAnswer((InvocationOnMock invocation) -> {
            for (int j = 0; j < responses.length; j++) {
                atManager.process(PEER_RESPONSE + j);
            }
            return null;
        }).when(outputStream).write(any());
        for (int j = 0; j < responses.length; j++) {
            ATParser.Result response = responses[j];
            when(atParser.process(PEER_RESPONSE + j)).thenReturn(response);
        }
    }

    @Test(expected = ATManager.AlreadyRegisteredException.class)
    public void canNotRegisterSameEventTwoTimes() throws ATManager.AlreadyRegisteredException {
        atManager.registerEvent(AN_EVENT, eventHandler);
        atManager.registerEvent(AN_EVENT, eventHandler);
    }

    @Test(expected = ATManager.AlreadyRegisteredException.class)
    public void canNotRegisterSameCommandTwoTimes() throws ATManager.AlreadyRegisteredException {
        atManager.registerCommand(A_COMMAND, aCommandHandler);
        atManager.registerCommand(A_COMMAND, aCommandHandler);
    }

    @Test(expected = ATManager.AlreadyRegisteredException.class)
    public void canNotRegisterEventIfACommandWithSameNameIsAlreadyRegistered() throws ATManager.AlreadyRegisteredException {
        atManager.registerCommand(A_COMMAND, aCommandHandler);
        atManager.registerEvent(A_COMMAND, eventHandler);
    }

    @Test(expected = ATManager.AlreadyRegisteredException.class)
    public void canNotRegisterCommandIfAnEventWithSameNameIsAlreadyRegistered() throws ATManager.AlreadyRegisteredException {
        atManager.registerEvent(AN_EVENT, eventHandler);
        atManager.registerCommand(AN_EVENT, aCommandHandler);
    }

    @Test
    public void canRegisterSameEventTwoTimesIfItIsUnregisteredBefore() throws ATManager.AlreadyRegisteredException {
        atManager.registerEvent(AN_EVENT, eventHandler);
        atManager.unregisterEvent(AN_EVENT);
        atManager.registerEvent(AN_EVENT, eventHandler);
    }

    @Test
    public void canRegisterSameCommandTwoTimesIfItIsUnregisteredBefore() throws ATManager.AlreadyRegisteredException {
        atManager.registerCommand(A_COMMAND, aCommandHandler);
        atManager.unregisterCommand(A_COMMAND);
        atManager.registerCommand(A_COMMAND, aCommandHandler);
    }

    @Test
    public void eventsSendToATManagerAppearInOutputStream() throws IOException {
        atManager.send(ATEvent.event(AN_EVENT, AN_EVENT_PARAMETERS));

        verify(outputStream).write(AN_EVENT_STRING.getBytes());
    }

    @Test
    public void atParserIsUsedToParseIncomingStrings() {
        atManager.process(AN_EVENT_STRING);

        verify(atParser).process(AN_EVENT_STRING);
    }

    @Test
    public void whenAnEventIsReceivedItsHandlerIsCalled() throws ATManager.AlreadyRegisteredException {
        atManager.registerEvent(AN_EVENT, eventHandler);

        atManager.process(AN_EVENT_STRING);

        verify(eventHandler).accept(any(ATEvent.class));
    }

    @Test
    public void whenACommandIsReceivedItsHandlerIsCalled() throws ATManager.AlreadyRegisteredException {
        atManager.registerCommand(A_COMMAND, aCommandHandler);

        atManager.process(A_COMMAND_STRING);

        verify(aCommandHandler).apply(A_COMMAND_AS_CLASS);
    }

    @Test
    public void whenAnUnregisteredCommandIsReceivedNoExceptionIsThrown() {
        atManager.process(A_COMMAND_STRING);
    }

    @Test
    public void erroneousStringsAreDiscarded() {
        when(atParser.process(anyString())).thenReturn(ATParser.Result.error("an error msg"));
        atManager.process("whatever");
    }

    @Test
    public void atLinesWithEmptyCommandSendsOKToTheWire() throws IOException {
        List<ATCommand> commands = Collections.singletonList(ATCommand.emptyCommand());
        when(atParser.process("AT")).thenReturn(ATParser.Result.commands(commands));

        atManager.process("AT");

        verify(outputStream).write("\r\nOK\r\n".getBytes());
    }

    @Test
    public void unregisteredCommandsSendsErrorToTheWire() throws IOException {
        atManager.process(A_COMMAND_STRING);

        verify(outputStream).write("\r\nERROR\r\n".getBytes());
    }

    @Test
    public void registeredCommandsSendsItsResponseToTheWire() throws IOException, ATManager.AlreadyRegisteredException {
        atManager.registerCommand(ANOTHER_COMMAND, anotherCommandHandler);
        when(anotherCommandHandler.apply(ANOTHER_COMMAND_AS_CLASS)).thenReturn(ATResponse.error(12));

        atManager.process(ANOTHER_COMMAND_STRING);

        verify(outputStream).write("\r\nERROR: 12\r\n".getBytes());
    }

    @Test(timeout = 500)
    public void sendSendsTheCommandToTheWireAndWaitsForResponse() throws IOException, ExecutionException, InterruptedException {
        peerWillRespondWith(ATParser.Result.completeResponseOk());

        ATResponse actual = atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        verify(outputStream).write(A_COMMAND_STRING.getBytes());
        assertTrue(actual.isOk());
    }

    @Test(timeout = 500)
    public void sendSetsAtParserInResponseMode() throws IOException {
        peerWillRespondWith(ATParser.Result.completeResponseOk());

        atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit);

        verify(atParser).setResponseMode(A_COMMAND);
    }

    @Test(timeout = 500)
    public void partialResponsesAreAccumulatedAndReturned() throws IOException, ExecutionException, InterruptedException {
        List<String> responseParameters1 = Arrays.asList("1", "2");
        List<String> responseParameters2 = Arrays.asList("3", "4");
        ATEvent event1 = ATEvent.event(A_COMMAND, responseParameters1);
        ATEvent event2 = ATEvent.event(A_COMMAND, responseParameters2);

        ATParser.Result p1 = ATParser.Result.partialResponse(A_COMMAND, responseParameters1);  // +RDCURR: 1,2\r\n
        ATParser.Result p2 = ATParser.Result.partialResponse(A_COMMAND, responseParameters2);  // +RDCURR: 3,4\r\n
        ATParser.Result p3 = ATParser.Result.completeResponseOk();                             // \r\nOK\r\n
        peerWillRespondWith(p1, p2, p3);

        ATResponse actual = atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        assertTrue(actual.getPartialResponses().contains(event1));
        assertTrue(actual.getPartialResponses().contains(event2));
    }

    @Test(timeout = 500)
    public void bodyLinesAreAccumulatedAndReturned() throws IOException, ExecutionException, InterruptedException {
        String line1 = "A line of text";
        String line2 = "Another text line";
        ATParser.Result p1 = ATParser.Result.bodyLine(line1);
        ATParser.Result p2 = ATParser.Result.bodyLine(line2);
        ATParser.Result p3 = ATParser.Result.completeResponseOk();
        peerWillRespondWith(p1, p2, p3);

        ATResponse actual = atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        assertEquals(actual.getBody(), String.join("\n", line1, line2));
    }

    @Test
    public void sendCommandAfterLastIsCompletedDoesNotCompleteWithError() throws IOException, ExecutionException, InterruptedException {
        String line1 = "A line of text";
        ATParser.Result p1 = ATParser.Result.bodyLine(line1);
        ATParser.Result p2 = ATParser.Result.completeResponseOk();
        peerWillRespondWith(p1, p2);

        atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        ATResponse actual = atManager.send(ANOTHER_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        assertTrue(actual.isOk());
    }

    @Test
    public void sendCommandBeforeLastIsCompletedATResponseError() throws ExecutionException, InterruptedException {
        atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit);

        ATResponse actual = atManager.send(ANOTHER_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        assertFalse(actual.isOk());
        assertNotNull(actual.getErrorMsg());
    }

    @Test
    public void timeoutCompleteWithErrorAndResetTheParser() throws ExecutionException, InterruptedException {
        ATResponse actual = atManager.send(A_COMMAND_AS_CLASS, TIMEOUT, unit).get();

        assertFalse(actual.isOk());
        assertEquals(actual.getErrorMsg(), "AT command timeout");
        verify(atParser).resetMode();
    }
}
