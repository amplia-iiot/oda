package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.ruleengine.api.ScriptTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class NashornScriptTranslator implements ScriptTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(es.amplia.oda.ruleengine.nashorn.NashornScriptTranslator.class);
    private static final String ENGINE_NAME = "nashorn";

    private HashMap<String,ScriptEngine> engines = new HashMap<>();

    @Override
    public void initScript(String script) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(ENGINE_NAME);
        engine.eval(readFile(script));
        engines.put(script, engine);
    }

    @Override
    public void putAttribute(String script, String attName, Object value) {
        this.engines.get(script).put(attName, value);
    }

    @Override
    public Object runMethod(String script, String method, Object... params) {
        try {
            Invocable rule = (Invocable) engines.get(script);
            params[0] = rule.invokeFunction(method, params[0], params[1]);
        } catch (ScriptException e) {
            LOGGER.error("Error trying to execute script " + script + " method " + method);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Method " + method + " doesn't exists on scipt " + script);
        }
        return params[0];
    }

    @Override
    public void close() {
        // With this library is not required, but in others objects have to be closed before stop the program.
        engines.clear();
    }

    private String readFile(String file) {
        try {
            Scanner script = new Scanner(new File(file));
            StringBuilder scriptContent = new StringBuilder();

            while(script.hasNext()) {
                scriptContent.append(script.nextLine());
            }

            script.close();

            return scriptContent.toString();
        } catch (FileNotFoundException ignored) {}
        return "";
    }
}
