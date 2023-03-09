package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.DatastreamInfo;
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
    private String jsUtilsPath;
    private HashMap<String, Rule> rules;
    private HashMap<String, DirectoryWatcher> watcher;

    private boolean started = false;

    private ScriptTranslator script;

    public RuleEngineNashorn(ScriptTranslator script) {
        this.script = script;
    }

    public void loadConfiguration(RuleEngineConfiguration config) {
        this.path = config.getPath();
        this.jsUtilsPath = config.getUtilsPath();
        this.rules = new HashMap<>();
        this.watcher = new HashMap<>();

        prepareMainDirectory();
        prepareJsUtilsDirectory();

        started = true;
    }

    @Override
    public synchronized State engine(State state, DatastreamValue value) {
        if (started) {

            List<String> rulesOfDatastream = rules.values().stream()
                    .filter(rule -> rule.getDatastreamIds().contains(value.getDatastreamId()))
                    .map(Rule::getName)
                    .collect(Collectors.toList());

            for (String rule : rulesOfDatastream) {
                try {
                    if (rules.get(rule).when(state, value)) {
                        LOGGER.info("Applying rule {} to datastream {}", rule, value.getDatastreamId());
                        state = rules.get(rule).then(state, value);
                    }
                } catch (ClassCastException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        state = checkRefreshedDatastream(state, value);
        LOGGER.info("Refreshed value of state");
        LOGGER.debug("Refreshed value of state with the datastream value {}", value);

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
        String fullPath = this.path + datastreamId;
        if (this.watcher.get(fullPath) != null) {
            this.watcher.get(fullPath).stop();
            this.watcher.remove(fullPath);
            List<String> keys = new ArrayList<>(this.rules.keySet());
            for (String key : keys) {
                if (key.contains(fullPath)) {
                    this.rules.remove(key);
                }
            }
            LOGGER.info("Deleted rules for datastream {}", datastreamId);
        }
    }

    @Override
    public void createRule(String nameRule) {
        File f = new File(nameRule);
        if (f.isFile()) {
            int index = nameRule.lastIndexOf(FileSystems.getDefault().getSeparator());
            String dirName = "";

            if (index != -1) {
                dirName = nameRule.substring(0, index);
            }

            initRuleScript(dirName, nameRule);
            LOGGER.info("Created rule {}", nameRule);
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
        LOGGER.info("Created directory {} for a new datastream", dir);
    }

    private void prepareJsUtilsDirectory() {
        this.watcher.put(this.jsUtilsPath, new RulesUtilsDirectoryWatcher(Paths.get(this.jsUtilsPath), this));
        this.watcher.get(this.jsUtilsPath).start();
    }

    @Override
    public void reloadAllRules() {

        try {
            Stream<Path> pathStreamDirectory = Files.list(Paths.get(this.path))
                    .filter(filePath -> filePath.toFile().isDirectory());
            List<String> directories = pathStreamDirectory.map(Path::toString).collect(Collectors.toList());
            directories.forEach(this::reloadRules);

            pathStreamDirectory.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void reloadRules(String dir) {
        try (Stream<Path> pathStreamFiles = Files.list(Paths.get(dir)).filter(filePath -> filePath.toFile().isFile())) {
            List<String> files = pathStreamFiles.map(Path::toString).filter(file -> file.endsWith(".js"))
                    .collect(Collectors.toList());

            for (String file : files) {
                initRuleScript(dir, file);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        LOGGER.info("Reloaded rules in directory {}", dir);
    }

    private void initRuleScript(String dirName, String nameRule) {

        // the name of the directory where the rules are stored are the ids of the datastreams affected
        // the datastreamIds are separated by ":"
        String currentPath = this.path;
        List<String> datastreamIds = Arrays.asList(dirName.substring(
                        dirName.indexOf(currentPath) + currentPath.length())
                .split(":"));

        try {
            this.rules.put(nameRule, new Rule(nameRule, datastreamIds, script));
        } catch (ScriptException e) {
           LOGGER.error("Cannot init rule {}: {}", nameRule, e.getMessage());
        }
    }

    private State checkRefreshedDatastream(State state, DatastreamValue newValue) {
        if(!state.exists(newValue.getDeviceId(), newValue.getDatastreamId())) {
            state.put(new DatastreamInfo(newValue.getDeviceId(), newValue.getDatastreamId()), newValue);
        }
        else if(!state.isRefreshed(newValue.getDeviceId(), newValue.getDatastreamId())) {
            return baseCase(state, newValue);
        }
        return state;
    }

    private State baseCase(State state, DatastreamValue newValue) {
        state.refreshValue(newValue.getDeviceId(), newValue.getDatastreamId(), newValue);
        return state;
    }

    public void stop() {
        LOGGER.info("Stopping the rule engine");
        started = false;
        this.rules.clear();
        this.watcher.forEach((s, directoryWatcher) -> directoryWatcher.stop());
        this.watcher.clear();
    }
}
