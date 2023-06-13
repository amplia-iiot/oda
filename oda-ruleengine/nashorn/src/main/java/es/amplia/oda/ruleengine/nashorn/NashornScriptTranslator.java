package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.ruleengine.api.ScriptTranslator;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
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
    private String jsUtilsPath;

    private HashMap<String,ScriptEngine> engines = new HashMap<>();

    public void loadConfiguration(RuleEngineConfiguration config) {
        this.jsUtilsPath = config.getUtilsPath();
    }

    @Override
    public void initScript(String script) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(ENGINE_NAME);

        // all rules will have preloaded all the functions from utils.js
        engine.eval("load('" + jsUtilsPath + "utils.js" + "')");

        // load rule
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
            LOGGER.error("Error trying to execute script {} method {}", script, method);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Method {} doesn't exists on script {}", method, script);
        }
        return params[0];
    }

    @Override
    public void close() {
        // With this library is not required, but in others objects have to be closed before stop the program.
        engines.clear();
    }

    private String readFile(String file) {
        try (Scanner script = new Scanner(new File(file), "UTF-8")) {
            StringBuilder scriptContent = new StringBuilder();

            while (script.hasNext()) {
                String nextLine = script.nextLine().trim();
                // ignore lines that starts with //, these are comments
                if(!nextLine.startsWith("//")) {
                    scriptContent.append(replaceLoadPath(nextLine));
                }
            }

            return scriptContent.toString();
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found {}", file);

        }
        return "";
    }

    private String replaceLoadPath(String stringToCheck) {
        // if rule contains lines to load another javascript, its path must be added
        // load("script.js") lines must be treated to add the path where script.js is stored
        // final result will be load("jslib/script.js")
        String loadString = "load(";

        if (stringToCheck.startsWith(loadString)) {

            // If javascript to load is util.js, return empty string
            // This adds compatibility with older versions where utils.js wasn't loaded internally
            if (stringToCheck.contains("/utils.js")) {
                return "";
            }

            String jsToLoad = stringToCheck.substring(stringToCheck.indexOf(loadString) + loadString.length() + 1);
            return loadString + "\"" + jsUtilsPath + jsToLoad;
        }

        return stringToCheck;
    }
}
