package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.api.ATCommand;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

/**
 * <p>ATParser is a state machine with two states:
 * <ul>
 * <li>Idle</li>
 * <li>Waiting Response</li>
 * </ul>
 * process(String line) function is used to received input data. Should be invoke with complete lines without \n.</p>
 * <p>It processes the following line types:
 * <ul>
 * <li>"" -&gt; Empty line. It return EMPTY. Spaces are not meaningful, so "   " is considered EMPTY also.</li>
 * <li>"AT..." -&gt; Line starting with "AT". Differentiates the next subtypes:
 * <ul>
 * <li>"AT&lt;char&gt; (e.g. "ATA" or "ATD917429966") -&gt; One character command. Character should be [A-Z] and does not admit parameters</li>
 * <li>"AT+... (e.g. "AT+CPIN=0000") -&gt; Multi character command with optional parameters
 * Command may be concatenated with ';'(e.g. "ATA;+CPIN=0000;")</li>
 * </ul></li>
 * <li>"+...." (e.g. "+WIND: 11,,,2") -&gt; Response type</li>
 * </ul></p>
 * <p>Any other line format results in type==LineType.ERROR and is discarded.</p>
 * <p>In the "waiting response" state the same line types are processed but, instead of discarding the lines with other formats,
 * it waits to read the next keywords: OK, ERROR, CONNECT, RING, NO CARRIER, NO DIALTONE, BUSY, NO ANSWER ó CONNECT.
 * This key words can be optionally preceded with "+CME" and followed by ": free format text".</p>
 * <p>When a line of this kind is received, it exits the "waiting response" state.</p>
 * <p>To transit to the "waiting response" state, the ATParser should received an explicit order to wait for response.
 * While the ATParser is in "waiting response" state, all responses are buffered until OK, ERROR, etc. is received.</p>
 * An example of the ATParser behaviour is the following:
 * <pre>
 * ==&gt; AT+CGACT?
 * &lt;==
 * &lt;== +CGACT: 1,1
 * &lt;== +CGACT: 2,0
 * &lt;== +CGACT: 3,0
 * &lt;==
 * &lt;== OK
 * </pre>
 * <p>Just before sending "AT+CGACT?" to peer, it transits to "Waiting Response" state storing the command to wait to "CGACT"
 * using setResponseMode(String cmd) function</p>
 * Execution:
 * <table border="2" cellpadding="4" cellspacing="0" summary="">
 * <tr>
 * <td> <b>Invoke function with parameters </b></td> <td> <b>LineType in response </b></td> <td> <b>State after method exits </b></td>
 * </tr>
 * <tr>
 * <td> Constructor </td> <td>  </td> <td> Idle </td>
 * </tr>
 * <tr>
 * <td> setResponseMode("CGACT") </td> <td> </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("") </td> <td> BODY_LINE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("+CGACT: 1,1") </td> <td> PARTIAL_RESPONSE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("+CGACT: 2,0") </td> <td> PARTIAL_RESPONSE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("+CGACT: 3,0") </td> <td> PARTIAL_RESPONSE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("") </td> <td> BODY_LINE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("OK") </td> <td> COMPLETE_RESPONSE </td> <td> Idle </td>
 * </tr>
 * </table>
 * <p>Last line make the state machine to transit to Idle state.</p>
 * Other scenarios while we are in "Waiting Response" are:
 * <table border="2" cellpadding="4" cellspacing="0" summary="">
 * <tr>
 * <td> <b>Invoke function with parameters </b></td> <td> <b>LineType in response </b></td>  <td> <b>State after method exits </b></td>
 * </tr>
 * <tr>
 * <td> process("+CGREG: 0") </td> <td> UNSOLICITED_RESPONSE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("AT+FOO") </td> <td> COMMANDS </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> process("657c09gg.Q24PL001 1956992 042407 11:29") </td> <td> BODY_LINE </td> <td> Waiting Response </td>
 * </tr>
 * <tr>
 * <td> setResponseMode(anyString) </td> <td> throws RuntimeException("...") </td> <td> Waiting Response </td>
 * </tr>
 * </table>
 * <p>First scenario is the arrived of a different response than the one we are waiting.</p>
 * <p>Second scenario shows that in "Waiting Response" state we can also received AT commands from peer.</p>
 * <p>Third scenario shows any text different from an AT command or response is buffered until an end line keywords is
 * received (OK, ERROR, ...)</p>
 * <p>Forth scenario shows a programming error: it is not allowed to transit to "Wait Response" state when we are already
 * in this state.</p>
 */
public interface ATParser {

    Result process(String line);

    void setResponseMode(String cmd);

    boolean isInResponseMode();

    void resetMode();

    enum LineType {
        EMPTY, ERROR, COMMANDS, UNSOLICITED_RESPONSE, PARTIAL_RESPONSE, BODY_LINE, COMPLETE_RESPONSE
    }

    @Value
    @AllArgsConstructor
    class Result {
        LineType type;
        List<ATCommand> commands;
        String errorMsg;

        String responseName;
        List<String> responseParameters;
        String body;
        String errorCodeInCompleteResponse;

        static Result empty() {
            return new Result(LineType.EMPTY, null, null, null, null, null, null);
        }

        public static Result error(String errorMsg) {
            return new Result(LineType.ERROR, null, errorMsg, null, null, null, null);
        }

        public static Result commands(List<ATCommand> commands) {
            return new Result(LineType.COMMANDS, commands, null, null, null, null, null);
        }

        static Result unsolicitedResponse(String responseName, List<String> responseParameters) {
            return new Result(LineType.UNSOLICITED_RESPONSE, null, null, responseName, responseParameters, null, null);
        }

        static Result unsolicitedResponse(String responseName, String... responseParameters) {
            return new Result(LineType.UNSOLICITED_RESPONSE, null, null, responseName, Arrays.asList(responseParameters), null, null);
        }

        static Result partialResponse(String responseName, List<String> responseParameters) {
            return new Result(LineType.PARTIAL_RESPONSE, null, null, responseName, responseParameters, null, null);
        }

        static Result completeResponseOk() {
            return new Result(LineType.COMPLETE_RESPONSE, null, null, null, null, null, null);
        }

        static Result completeResponseError(String errorLineInCompleteResponse) {
            return new Result(LineType.COMPLETE_RESPONSE, null, null, null, null, null, errorLineInCompleteResponse);
        }

        static Result bodyLine(String line) {
            return new Result(LineType.BODY_LINE, null, null, null, null, line, null);
        }

        boolean isCompleteResponseOk() {
            return errorCodeInCompleteResponse == null;
        }
    }
}
