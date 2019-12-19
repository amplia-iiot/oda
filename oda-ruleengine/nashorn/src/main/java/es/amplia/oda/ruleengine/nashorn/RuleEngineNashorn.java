package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.ruleengine.api.*;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleEngineNashorn implements es.amplia.oda.ruleengine.api.RuleEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleEngineNashorn.class);

    private String path;
    private HashMap<String, Rule> rules;
    private HashMap<String, DirectoryWatcher> watcher;

    private boolean started = false;

    private ScriptTranslator script;

    public RuleEngineNashorn(ScriptTranslator script) {
        this.script = script;
    }

    public void loadConfiguration(RuleEngineConfiguration config) {
        this.path = config.getPath();
        this.rules = new HashMap<>();
        this.watcher = new HashMap<>();

        prepareMainDirectory();

        started = true;
    }

    @Override
    public State engine(State state, DatastreamValue value) {
        if (started && this.watcher.get(this.path + value.getDatastreamId()) != null) {
            List<String> rulesOfDatastream = rules.keySet().stream()
                    .filter(rulename -> rulename.contains(this.path + value.getDatastreamId()))
                    .collect(Collectors.toList());
            for (String rule : rulesOfDatastream) {
                try {
                    if (rules.get(rule).when(state, value)) {
                        state = rules.get(rule).then(state, value);
                    }
                } catch (ClassCastException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        state = checkRefreshedDatastream(state, value);
        LOGGER.info("Refreshed value of {} to {}", value.getDatastreamId(), state.getValue(value.getDatastreamId()));

        return state;
    }

    @Override
    public void createDatastreamDirectory(String datastreamId) {
        File f = new File(this.path + datastreamId);
        if(f.isDirectory()) {
            prepareDatastreamDirectory(this.path + datastreamId);
        }
    }

    @Override
    public void deleteDatastreamDirectory(String datastreamId) {
        if (this.watcher.get(datastreamId) != null) {
            this.watcher.get(datastreamId).stop();
            this.watcher.remove(datastreamId);
            List<String> keys = new ArrayList<>(this.rules.keySet());
            for (String key : keys) {
                if (key.contains(path + datastreamId)) {
                    this.rules.remove(key);
                }
            }
        }
    }

    @Override
    public void createRule(String nameRule) {
        File f = new File(nameRule);
        if(f.isFile()) {
            int index = nameRule.lastIndexOf('/');
            String datastreamId = "";

            if (index != -1) {
                datastreamId = nameRule.substring(0, index);
            }

            datastreamId = datastreamId.replaceFirst("rules/", "");

            initRuleScript(datastreamId, nameRule);
        }
    }

    @Override
    public void deleteRule(String nameRule) {
        this.rules.remove(nameRule);
    }

    private void prepareMainDirectory() {
        Stream<Path> pathStreamDirectory;

        this.watcher.put(this.path, new MainDirectoryWatcher(Paths.get(this.path), this));

        try {
            pathStreamDirectory = Files.list(Paths.get(this.path)).filter(filePath -> filePath.toFile().isDirectory());
            List<String> directories = pathStreamDirectory.map(Path::toString).collect(Collectors.toList());
            directories.forEach(this::prepareDatastreamDirectory);

            pathStreamDirectory.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        this.watcher.get(this.path).start();
    }

    private void prepareDatastreamDirectory(String dir) {

        this.watcher.put(dir, new RulesDirectoryWatcher(Paths.get(dir), this));
        try (Stream<Path> pathStreamFiles = Files.list(Paths.get(dir)).filter(filePath -> filePath.toFile().isFile())) {
            List<String> files = pathStreamFiles.map(Path::toString).filter(file -> file.endsWith(".js")).collect(Collectors.toList());

            for (String file : files) {
                initRuleScript(dir, file);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        this.watcher.get(dir).start();
    }

    private void initRuleScript(String datastreamId, String nameRule) {
        try {
            this.rules.put(nameRule, new Rule(nameRule, datastreamId, script));
        } catch (ScriptException e) {
            LOGGER.error("Cannot init rule {}: {}", nameRule, e.getMessage());
        }
    }

    private State checkRefreshedDatastream(State state, DatastreamValue newValue) {
        if(!state.isRefreshed(newValue.getDatastreamId())) {
            return baseCase(state, newValue);
        }
        return state;
    }

    private State baseCase(State state, DatastreamValue newValue) {
        state.refreshValue(newValue.getDatastreamId(), newValue);
        return state;
    }

    public void stop() {
        started = false;
        this.rules.clear();
        this.watcher.forEach((s, directoryWatcher) -> directoryWatcher.stop());
        this.watcher.clear();
    }
}
