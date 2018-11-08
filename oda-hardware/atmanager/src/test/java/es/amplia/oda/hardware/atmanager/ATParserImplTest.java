package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.ATParser;
import es.amplia.oda.hardware.atmanager.ATParserImpl;
import es.amplia.oda.hardware.atmanager.api.ATCommand;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class ATParserImplTest {

    private ATParser atParser;

    @Before
    public void setup() {
        atParser = new ATParserImpl();
    }

    @Test
    public void emptyString() {
        ATParser.Result actual = atParser.process("");

        Assert.assertEquals(actual, ATParser.Result.empty());
    }

    @Test
    public void startSpacesAreDiscarded() {
        ATParser.Result actual = atParser.process("  \r  \r  ");

        Assert.assertEquals(actual, ATParser.Result.empty());
    }

    @Test
    public void linesThatNotBeginWithATNorPlusAreErroneous() {
        ATParser.Result actual = atParser.process("ERR");

        ATParser.Result expected = ATParser.Result.error("Unrecognized line");
        assertEquals(actual, expected);
    }

    @Test
    public void aLineWithOnlyATisCorrect() {
        ATParser.Result actual = atParser.process("AT");

        ATParser.Result expected = ATParser.Result.commands(Collections.singletonList(ATCommand.emptyCommand()));
        assertEquals(actual, expected);
    }

    @Test
    public void inputIsUppercased() {
        ATParser.Result actual = atParser.process("at");

        ATParser.Result expected = ATParser.Result.commands(Collections.singletonList(ATCommand.emptyCommand()));
        assertEquals(actual, expected);
    }

    @Test
    public void errorsInCommandAreReportedAsAnError() {
        ATParser.Result actual = atParser.process("AT+P+");

        ATParser.Result expected = ATParser.Result.error("Unexpected token 'ERROR'");
        assertEquals(actual, expected);
    }

    @Test
    public void errorsInResponsesAreReportedAsAnError() {
        ATParser.Result actual = atParser.process("+CREG:");

        ATParser.Result expected = ATParser.Result.error("Unexpected end of line");
        assertEquals(actual, expected);
    }

    @Test
    public void responseModeIsCorrectlyIndicated() {
        assertFalse(atParser.isInResponseMode());
        atParser.setResponseMode("+CGACT");
        assertTrue(atParser.isInResponseMode());
    }

    @Test(expected = RuntimeException.class)
    public void inResponseModeChangingToResponseModeAgainThrowsException() {
        atParser.setResponseMode("+CGACT");

        atParser.setResponseMode("+CPIN");
    }

    @Test
    public void inResponseModeResponsesForTheCommandAreReportedAsPartialResponses() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("+CGACT: 1,1");

        ATParser.Result expected = ATParser.Result.partialResponse("+CGACT", Arrays.asList("1", "1"));
        assertEquals(actual, expected);
    }

    @Test
    public void inResponseModeFreeTextIsReportedAsBodyLine() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process(" \r 657c09gg.Q24PL001 1956992 042407 11:29 \r ");

        ATParser.Result expected = ATParser.Result.bodyLine("657c09gg.Q24PL001 1956992 042407 11:29");
        assertEquals(actual, expected);
    }

    @Test
    public void inResponseModeEmptyLinesAreReportedAsBodyLine() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process(" \r    \r ");

        ATParser.Result expected = ATParser.Result.bodyLine("");
        assertEquals(actual, expected);
    }

    @Test
    public void inResponseModeLineWithOKEndsResponseMode() {
        atParser.setResponseMode("+CGACT");

        atParser.process("OK");

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void spacesAreNotSignificantToEndResponseMode() {
        atParser.setResponseMode("+CGACT");

        atParser.process("  \r  OK  \r  ");

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void caseIsNotSignificantToEndResponseMode() {
        atParser.setResponseMode("+CGACT");

        atParser.process("oK");

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void inResponseModeLineWithErrorEndsResponseMode() {
        atParser.setResponseMode("+CGACT");

        atParser.process("error");

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void inResponseModeLineWithErrorAndCodeEndsResponseMode() {
        atParser.setResponseMode("+CGACT");

        atParser.process("+cme   error: 13");

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void inResponseModeReceivedResponseForAnotherCommandIsAnUnsolicitedResponse() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("+FOO: 12");

        ATParser.Result expected = ATParser.Result.unsolicitedResponse("+FOO", Collections.singletonList("12"));
        assertEquals(actual, expected);
    }

    @Test
    public void inResponseModeReceivedATIsACommand() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("AT+FOO=12");

        ATParser.Result expected = ATParser.Result.commands(Collections.singletonList(ATCommand.extendedSetCommand("+FOO", "12")));
        assertEquals(actual, expected);
    }

    @Test
    public void inResponseModeResetResponseMode() {
        atParser.setResponseMode("+CGACT");

        atParser.resetMode();

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void inResponseModeResetResponseModeAndSetItAgainDoesNotThrowException() {
        atParser.setResponseMode("+CGACT");

        atParser.resetMode();

        atParser.setResponseMode("+CPIN");

        assertTrue(atParser.isInResponseMode());
    }

    @Test
    public void inNormalModeResetResponseModeDoesNotThrowException() {
        atParser.resetMode();

        assertFalse(atParser.isInResponseMode());
    }

    @Test
    public void whenResponseModeEndsCompleteResponseIsReturned() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("oK");

        Assert.assertEquals(actual, ATParser.Result.completeResponseOk());
    }

    @Test
    public void anOkLineIsInformedAsOK() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("oK");

        assertTrue(actual.isCompleteResponseOk());
    }

    @Test
    public void anErrorLineWithErrorNumber() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("error");

        assertFalse(actual.isCompleteResponseOk());
        assertEquals(actual.getErrorCodeInCompleteResponse(), "");
    }

    @Test
    public void anErrorLineWithErrorString() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("error: \"a Text\"");

        assertFalse(actual.isCompleteResponseOk());
        assertEquals(actual.getErrorCodeInCompleteResponse(), "a Text");
    }

    @Test
    public void anErrorLineNumberIsInformed() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("+cme error: 12");

        assertFalse(actual.isCompleteResponseOk());
        assertEquals(actual.getErrorCodeInCompleteResponse(), "12");
    }

    @Test
    public void completeExample() {
        atParser.setResponseMode("+CGACT");

        ATParser.Result actual = atParser.process("");
        ATParser.Result expected = ATParser.Result.bodyLine("");
        assertEquals(actual, expected);


        actual = atParser.process("+CGACT: 1,1");
        expected = ATParser.Result.partialResponse("+CGACT", Arrays.asList("1", "1"));
        assertEquals(actual, expected);

        actual = atParser.process("+CGACT: 2,0");
        expected = ATParser.Result.partialResponse("+CGACT", Arrays.asList("2", "0"));
        assertEquals(actual, expected);

        actual = atParser.process("+CGREG: 0");
        expected = ATParser.Result.unsolicitedResponse("+CGREG", Collections.singletonList("0"));
        assertEquals(actual, expected);

        actual = atParser.process("AT+FOO=12");
        expected = ATParser.Result.commands(Collections.singletonList(ATCommand.extendedSetCommand("+FOO", "12")));
        assertEquals(actual, expected);

        actual = atParser.process("+CGACT: 2,0");
        expected = ATParser.Result.partialResponse("+CGACT", Arrays.asList("2", "0"));
        assertEquals(actual, expected);

        actual = atParser.process("");
        expected = ATParser.Result.bodyLine("");
        assertEquals(actual, expected);

        actual = atParser.process("OK");
        expected = ATParser.Result.completeResponseOk();
        assertEquals(actual, expected);
    }
}
