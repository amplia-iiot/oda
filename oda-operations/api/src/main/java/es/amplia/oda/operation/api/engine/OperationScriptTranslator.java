package es.amplia.oda.operation.api.engine;

import javax.script.ScriptException;

public interface OperationScriptTranslator {
    void initScript(String script) throws ScriptException;
    Object runMethod(String script, String method, Object... params);
    void close();
}
