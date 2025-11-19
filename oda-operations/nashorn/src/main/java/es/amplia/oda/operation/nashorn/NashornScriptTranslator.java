package es.amplia.oda.operation.nashorn;

import es.amplia.oda.operation.api.engine.OperationScriptTranslator;
import es.amplia.oda.operation.nashorn.configuration.OperationEngineConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

@Slf4j
public class NashornScriptTranslator implements OperationScriptTranslator {

    private static final String ENGINE_NAME = "nashorn";
    private String jsUtilsPath;

    private final HashMap<String,ScriptEngine> engines = new HashMap<>();

    public void loadConfiguration(OperationEngineConfiguration config) {
        this.jsUtilsPath = config.getUtilsPath();
    }

    @Override
    public void initScript(String script) throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName(ENGINE_NAME);

        // all rules will have preloaded all the functions from utils.js
        //engine.eval("load('" + jsUtilsPath + "utils.js" + "')");

        // load rule
        engine.eval(readFile(script));

        engines.put(script, engine);
    }

    @Override
    public Object runMethod(String script, String method, Object... params) {
        try {
            Invocable rule = (Invocable) engines.get(script);
            params[0] = rule.invokeFunction(method, params);
        } catch (ScriptException e) {
            log.error("Error trying to execute script " + script + " method " + method, e);
        } catch (NoSuchMethodException e) {
            log.error("Method {} doesn't exists on script {}", method, script);
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
                if (!nextLine.startsWith("//")) {
                    scriptContent.append(replaceLoadPath(nextLine));
                }
            }

            return scriptContent.toString();
        } catch (FileNotFoundException e) {
            log.error("File not found {}", file);
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
