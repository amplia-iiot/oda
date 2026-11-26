package es.amplia.oda.core.commons.exceptions;

/**
 * Failure while executing a method of a JavaScript rule or operation.
 *
 * Carries the script and method that failed plus the original cause, so the engines can report
 * which script broke instead of surfacing an unrelated ClassCastException.
 */
public class ScriptExecutionException extends RuntimeException {

    private final String script;
    private final String method;

    /**
     * Constructor.
     * @param script Script that was being executed.
     * @param method Method of the script that was being invoked.
     * @param cause Original failure.
     */
    public ScriptExecutionException(String script, String method, Throwable cause) {
        super("Error executing method " + method + " of script " + script, cause);
        this.script = script;
        this.method = method;
    }

    public String getScript() {
        return script;
    }

    public String getMethod() {
        return method;
    }
}
