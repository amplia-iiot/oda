package es.amplia.oda.operation.nashorn;

import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.core.commons.utils.operation.response.Step;
import es.amplia.oda.core.commons.utils.operation.response.StepResultCode;
import es.amplia.oda.operation.api.engine.OperationDirectoryWatcher;
import es.amplia.oda.operation.api.engine.OperationEngine;
import es.amplia.oda.operation.api.engine.OperationNotFound;
import es.amplia.oda.operation.api.engine.OperationScript;
import es.amplia.oda.operation.api.engine.OperationScriptTranslator;
import es.amplia.oda.operation.nashorn.configuration.OperationEngineConfiguration;
import es.amplia.oda.core.commons.utils.OsgiContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperationEngineNashorn implements OperationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationEngineNashorn.class);

    private String path;
    private HashMap<String, OperationScript> operations;
    private OperationScriptTranslator script;
    private OperationDirectoryWatcher operationsWatcher;
    private OperationDirectoryWatcher operationsUtilsWatcher;


    public OperationEngineNashorn(OperationScriptTranslator script) {
        this.script = script;
    }

    public void loadConfiguration(OperationEngineConfiguration config) {
        this.path = config.getPath();
        this.operations = new HashMap<>();
        this.operationsWatcher = new OperationDirectoryWatcher(Paths.get(this.path), this);
        this.operationsUtilsWatcher = new OperationDirectoryWatcher(Paths.get(config.getUtilsPath()), this);

        reloadAllOperations();
        this.operationsWatcher.start();
        this.operationsUtilsWatcher.start();
    }

    @Override
    public Response engine(String operationName, String deviceId, String operationId, Map<String, Object> params, OsgiContext ctx) {
        OperationScript operation = this.operations.get(operationName);
        if (operation == null) throw new OperationNotFound("Operation " + operationName + " not found in Operation Engine");
        Map<String, Object> ret = operation.execute(deviceId, operationId, params, ctx);
        Response resp = null;
        if (ret != null) resp = new Response((ret.get("id")==null?operationId:(String)ret.get("id")),
                                ret.get("deviceId")==null?deviceId:(String)ret.get("deviceId"),
                                ret.get("path")==null?null:(String[])ret.get("path"),
                                operationName,
                                ret.get("resultCode")==null?null:OperationResultCode.valueOf((String)ret.get("resultCode")),
                                ret.get("resultDescription")==null?null:(String)ret.get("resultDescription"),
                                getSteps(ret.get("steps")));
        LOGGER.trace("Operation " + operationName + " responsed with: " + resp);
        return resp;
    }

    private List<Step> getSteps(Object steps) {
        if (steps == null) return null;
        List<Object> stepsList = ((Map<String,Object>)steps).values().stream().collect(Collectors.toList());
        return stepsList.stream().map(this::getStep).collect(Collectors.toList());
    }

    private Step getStep(Object stepObject) {
        Map<String, Object> step = (Map<String, Object>) stepObject;
        return new Step((String) step.get("name"), StepResultCode.valueOf((String) step.get("result")),
                (String) step.get("description"), (Long) step.get("timestamp"), (List<Object>) step.get("response"));
    }

    @Override
    public void reloadAllOperations() {
        this.operations.clear();
        LOGGER.info("Reloading operations in directory {}", this.path);
        try (Stream<Path> pathStreamFiles = Files.list(Paths.get(this.path)).filter(filePath -> filePath.toFile().isFile())) {
            List<String> files = pathStreamFiles.map(Path::toString).filter(file -> file.endsWith(".js"))
                    .collect(Collectors.toList());

            for (String file : files) {
                initOperationScript(file);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void initOperationScript(String operationFile) {
        try {
            String opName = operationFile.replace(".js", "").replace(this.path, "");
            this.operations.put(opName, new OperationScript(operationFile, script));
            LOGGER.info("Adding javascript operation " + opName);
        } catch (ScriptException e) {
           LOGGER.error("Cannot init javascript operation {}: {}", operationFile, e.getMessage());
        }
    }

    public void stop() {
        LOGGER.info("Stopping the operation engine");
        this.operations.clear();
        this.operationsWatcher.stop();
        this.operationsUtilsWatcher.stop();
    }
}
