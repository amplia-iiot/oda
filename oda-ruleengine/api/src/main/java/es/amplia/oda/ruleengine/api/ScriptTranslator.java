package es.amplia.oda.ruleengine.api;

import javax.script.ScriptException;

public interface ScriptTranslator {
    void initScript(String script) throws ScriptException;
    void putAttribute(String script, String attName, Object value);
    Object runMethod(String script, String method, Object... params);
    void close();
}
