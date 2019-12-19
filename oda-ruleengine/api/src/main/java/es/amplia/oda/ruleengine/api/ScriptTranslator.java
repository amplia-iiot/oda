package es.amplia.oda.ruleengine.api;

import javax.script.ScriptException;

public interface ScriptTranslator {
    /**
     * First method to call. Is like a constructor that initialize the script to use.
     * @param script String that contains the content of the script file
     * @return true iff the script is charged correctly
     */
    void initScript(String script) throws ScriptException;

    /**
     *
     * @param script
     * @param attName
     * @param value
     */
    void putAttribute(String script, String attName, Object value);

    /**
     *
     * @param script
     * @param method
     * @param params
     * @return
     */
    Object runMethod(String script, String method, Object... params);

    /**
     *
     */
    void close();
}
