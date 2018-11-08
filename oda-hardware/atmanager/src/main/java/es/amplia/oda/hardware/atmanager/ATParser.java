package es.amplia.oda.hardware.atmanager;

import es.amplia.oda.hardware.atmanager.api.ATCommand;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

/**
 * Un ATParser es una máquina de estados con dos estados:
 * <ul>
 * <li>normal</li>
 * <li>esperando respuesta</li>
 * </ul>
 * Se utiliza la función {@link process process(String line)} para recibir los datos de entrada. Debe invocarse con lineas completas y sin el \n.<p>
 * <p>
 * Hoy por hoy diferenciamos los siguientes tipos de líneas:
 * <ul>
 * <li>"" -&gt; Es decir, cadena vacía. Se devolverá EMPTY. Los espacios en blanco no son significativos, así que "   " también se considera EMPTY.</li>
 * <li>"AT..." -&gt; Es decir, cadena que empieza por "AT". De este tipo encontramos los subtipos:
 * <ul>
 * <li>"AT&lt;char&gt; (por ejemplo "ATA" ó "ATD917429966") -&gt; Comando de un sólo caracter. Este caracter debe ser [A-Z] ¿y no admite parámetros?.</li>
 * <li>"AT+... (por ejemplo "AT+CPIN=0000") -&gt; Comando multicaracter con parámetros opcionales
 * Pero es que además los comandos se pueden concatenar. De forma que "ATA;+CPIN=0000;" es válido.</li>
 * </ul></li>
 * <li>"+...." (por ejemplo "+WIND: 11,,,2") -&gt; tipo "response"</li>
 * <li>(Según el estándard, deberíamos ser capaces también de parsear un cuarto tipo de línea, la "A/",
 * que indica volver a ejecutar el último comando, pero por ahora no está)</li>
 * </ul>
 * Cualquier otra cosa se desprecia devolviendo un resultado con type==LineType.ERROR.<p>
 * <p>
 * En el estado "esperando respuesta" se parsean esas mismas líneas pero en vez de despreciar el resto de líneas
 * se espera una línea con cualquiera de estas subcadenas: OK, ERROR, CONNECT, RING, NO CARRIER,
 * NO DIALTONE, BUSY, NO ANSWER ó CONNECT, opcionalmente precedidas de "+CME " y opcionalmente seguidas de
 * ": texto libre". Por ejemplo "+CME ERROR: 16" también se considera.<p>
 * Al recibir una de esas líneas, se sale del modo "esperando respuesta".<p>
 * Para pasar al modo "esperando respuesta", hay que indicar de qué comando se está esperando la respuesta.
 * Ese comando recibe un tratamiento especial: mientras se siga en este estado, todas las respuestas a ese comando
 * se almacenarán en un buffer y cuando se reciba el OK, ERROR, etc, se devolverán.<p>
 * Este modo está pensado para tratar la respuesta a comandos ejemplarizados por "AT+CGACT?". El flujo de datos
 * para este comando es:
 * <pre>
 * ==&gt; AT+CGACT?
 * &lt;==
 * &lt;== +CGACT: 1,1
 * &lt;== +CGACT: 2,0
 * &lt;== +CGACT: 3,0
 * &lt;==
 * &lt;== OK
 * </pre>
 * Justo antes de enviar el comando "AT+CGACT?" al peer, se debe pasar al modo "esperando respuesta" indicando que
 * el comando por el que se espera es "CGACT". Se usa para ello la función {@link setResponseMode setResponseMode(String cmd)}.<p>
 * El funcionamiento será:
 * <table border="2" cellpadding="4" cellspacing="0" summary="">
 * <tr>
 * <td> <b>Función invocada y parámetros pasados </b></td> <td> <b>LineType en respuesta </b></td> <td> <b>Estado al salir de la función </b></td>
 * </tr>
 * <tr>
 * <td> Constructor </td> <td>  </td> <td> Modo normal </td>
 * </tr>
 * <tr>
 * <td> setResponseMode("CGACT") </td> <td> </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("") </td> <td> BODY_LINE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("+CGACT: 1,1") </td> <td> PARCIAL_RESPONSE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("+CGACT: 2,0") </td> <td> PARCIAL_RESPONSE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("+CGACT: 3,0") </td> <td> PARCIAL_RESPONSE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("") </td> <td> BODY_LINE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("OK") </td> <td> COMPLETE_RESPONSE </td> <td> Modo normal </td>
 * </tr>
 * </table>
 * La última línea hace además que se salga del estado "esperando respuesta".<p>
 * Por completar el escenario anterior, estado en "esperando respuesta" nos podríamos encontrar
 * estos otros casos:
 * <table border="2" cellpadding="4" cellspacing="0" summary="">
 * <tr>
 * <td> <b>Función invocada y parámetros pasados </b></td> <td> <b>LineType en respuesta </b></td>  <td> <b>Estado al salir de la función </b></td>
 * </tr>
 * <tr>
 * <td> process("+CGREG: 0") </td> <td> UNSOLICITED_RESPONSE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("AT+FOO") </td> <td> COMMANDS </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> process("657c09gg.Q24PL001 1956992 042407 11:29") </td> <td> BODY_LINE </td> <td> Modo respuesta </td>
 * </tr>
 * <tr>
 * <td> setResponseMode(anyString) </td> <td> throws RuntimeException("...") </td> <td> Modo respuesta </td>
 * </tr>
 * </table>
 * El primer caso ejemplifica la recepción de una respuesta diferente a la solicitada.<p>
 * El segundo caso muestra que en modo respuesta también se pueden recibir peticiones para ejecutar comandos AT.<p>
 * El tercer caso muestra que cualquier texto que no sea un comando AT o una respuesta se acumula en un buffer interno
 * para ser devuelto cuando se reciba la cadena que indica terminación de modo respueta (OK, ERROR, ...)<p>
 * El cuarto caso señala un problema de programación: no se debe intentar pasar a modo respuesta estando ya en
 * modo respuesta.<p>
 * Existe un conjunto de instrucciones AT que tienen respuesta pero que no terminan con una cadena OK, ERROR, ...
 * El caso que lo ejemplifica es el comando "CPIN":
 * <pre>
 * ==&gt; AT+CPIN=?
 * &lt;== OK
 *
 * ==&gt; AT+CPIN?
 * &lt;== +CPIN: SIM PIN
 * ¡Sin OK ni ERROR!
 *
 * ==&gt; AT+CPIN=5284
 * &lt;== OK
 *
 * ==&gt; AT+CPIN?
 * &lt;== +CPIN: READY
 * ¡Sin OK ni ERROR!
 * </pre>
 * El modo READ del comando "CPIN" no termina con OK ó ERROR. Termina en cuando se devuelve una respuesta parcial.<p>
 * Hoy por hoy no soportamos ese tipo de comandos.
 */
public interface ATParser {

    Result process(String line);

    void setResponseMode(String cmd);

    boolean isInResponseMode();

    void resetMode();

    public static enum LineType {
        EMPTY, ERROR, COMMANDS, UNSOLICITED_RESPONSE, PARTIAL_RESPONSE, BODY_LINE, COMPLETE_RESPONSE;
    }

    @Value
    @AllArgsConstructor
    public static class Result {
        LineType type;
        List<ATCommand> commands;
        String errorMsg;

        String responseName;
        List<String> responseParameters;
        String body;
        String errorCodeInCompleteResponse;

        public static Result empty() {
            return new Result(LineType.EMPTY, null, null, null, null, null, null);
        }

        public static Result error(String errorMsg) {
            return new Result(LineType.ERROR, null, errorMsg, null, null, null, null);
        }

        public static Result commands(List<ATCommand> commands) {
            return new Result(LineType.COMMANDS, commands, null, null, null, null, null);
        }

        public static Result unsolicitedResponse(String responseName, List<String> responseParameters) {
            return new Result(LineType.UNSOLICITED_RESPONSE, null, null, responseName, responseParameters, null, null);
        }

        public static Result unsolicitedResponse(String responseName, String... responseParameters) {
            return new Result(LineType.UNSOLICITED_RESPONSE, null, null, responseName, Arrays.asList(responseParameters), null, null);
        }

        public static Result partialResponse(String responseName, List<String> responseParameters) {
            return new Result(LineType.PARTIAL_RESPONSE, null, null, responseName, responseParameters, null, null);
        }

        public static Result completeResponseOk() {
            return new Result(LineType.COMPLETE_RESPONSE, null, null, null, null, null, null);
        }

        public static Result completeResponseError(String errorLineInCompleteResponse) {
            return new Result(LineType.COMPLETE_RESPONSE, null, null, null, null, null, errorLineInCompleteResponse);
        }

        public static Result bodyLine(String line) {
            return new Result(LineType.BODY_LINE, null, null, null, null, line, null);
        }

        boolean isCompleteResponseOk() {
            return errorCodeInCompleteResponse == null;
        }
    }
}
