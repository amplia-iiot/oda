package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.exceptions.ScriptExecutionException;
import es.amplia.oda.operation.api.engine.OperationScriptTranslator;
import es.amplia.oda.operation.nashorn.configuration.OperationEngineConfiguration;
import es.amplia.oda.ruleengine.api.ScriptTranslator;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Single nashorn script translator shared by the rule engine and the operation engine
 * (previously duplicated in two bundles). Implements both service-specific interfaces; the
 * only behavioural difference between the two engines is whether {@code utils.js} is preloaded
 * into every script, controlled by {@link #preloadUtils} (rules: yes; operations: no).
 */
@Slf4j
public class NashornScriptTranslator implements ScriptTranslator, OperationScriptTranslator {

    // Nashorn ya no forma parte del JDK (JEP 372): el ServiceLoader de ScriptEngineManager no lo
    // encuentra dentro de OSGi, así que se instancia la factoría del nashorn-core embebido
    private static final NashornScriptEngineFactory ENGINE_FACTORY = new NashornScriptEngineFactory();
    private String jsUtilsPath;

    private final boolean preloadUtils;
    private final HashMap<String,ScriptEngine> engines = new HashMap<>();

    /** Default translator preloads utils.js (rule engine behaviour). */
    public NashornScriptTranslator() {
        this(true);
    }

    public NashornScriptTranslator(boolean preloadUtils) {
        this.preloadUtils = preloadUtils;
    }

    public void loadConfiguration(RuleEngineConfiguration config) {
        this.jsUtilsPath = config.getUtilsPath();
    }

    public void loadConfiguration(OperationEngineConfiguration config) {
        this.jsUtilsPath = config.getUtilsPath();
    }

    @Override
    public void initScript(String script) throws ScriptException {
        ScriptEngine engine = ENGINE_FACTORY.getScriptEngine();

        // rules preload all the functions from utils.js; operations do not (legacy behaviour)
        if (preloadUtils) {
            engine.eval("load('" + jsUtilsPath + "utils.js" + "')");
        }

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
        Invocable rule = (Invocable) engines.get(script);
        try {
            // No se asigna a params[0]: si invokeFunction lanza, devolver params[0] entregaria el
            // primer parametro de entrada (el State en las reglas, el deviceId en las operaciones)
            // como si fuera el resultado del script
            return rule.invokeFunction(method, params);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new ScriptExecutionException(script, method, e);
        }
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
