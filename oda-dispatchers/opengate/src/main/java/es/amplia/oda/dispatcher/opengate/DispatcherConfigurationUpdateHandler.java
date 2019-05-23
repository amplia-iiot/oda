package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class DispatcherConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherConfigurationUpdateHandler.class);

    static final String REDUCE_BANDWIDTH_PROPERTY_NAME = "reduceBandwidthMode";

    private final ScheduledExecutorService executor;
    private final OpenGateEventDispatcher eventDispatcher;
    private final Scheduler scheduler;

    private boolean reduceBandwidthMode = false;
    private final Map<DispatchConfiguration, Set<String>> currentConfiguration = new HashMap<>();
    private final List<ScheduledFuture> configuredTasks = new ArrayList<>();

    DispatcherConfigurationUpdateHandler(ScheduledExecutorService executor, OpenGateEventDispatcher eventDispatcher,
                                         Scheduler scheduler) {
        this.executor = executor;
        this.eventDispatcher = eventDispatcher;
        this.scheduler = scheduler;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("OpenGate Dispatcher updated with {} properties", props.size());

        Enumeration<String> e = props.keys();
        while(e.hasMoreElements()) {
            String key = e.nextElement();
            String value = (String) props.get(key);

            if (key.equals(REDUCE_BANDWIDTH_PROPERTY_NAME)) {
                setReduceBandwidthMode(value);
            } else {
                parseDispatchConfiguration(key, value);
            }
        }
    }

    private void setReduceBandwidthMode(String value) {
        reduceBandwidthMode = Boolean.parseBoolean(value);
    }

    private void parseDispatchConfiguration(String key, String value) {
        try {
            String[] valueFields = value.split("\\s*(;\\s*)");
            long secondsFirstDispatch;
            long secondsBetweenDispatches;
            if (valueFields.length == 1) {
                secondsFirstDispatch = Long.parseLong(valueFields[0]);
                secondsBetweenDispatches = secondsFirstDispatch;
            } else {
                secondsFirstDispatch = Long.parseLong(valueFields[0]);
                secondsBetweenDispatches = Long.parseLong(valueFields[1]);
            }
            add(new DispatchConfiguration(secondsFirstDispatch, secondsBetweenDispatches), key);
        } catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            LOGGER.info("Rejecting configuration '{}={}' because is invalid: {}", key, value, ex);
        }
    }

    private void add(DispatchConfiguration dispatchConfiguration, String id) {
        Set<String> set = currentConfiguration.computeIfAbsent(dispatchConfiguration, k -> new HashSet<>());
        set.add(id);
    }

    @Override
    public void loadDefaultConfiguration(){
        currentConfiguration.clear();
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("Applying new OpenGate Dispatcher configuration");

        configuredTasks.forEach(task -> task.cancel(false));
        configuredTasks.clear();

        eventDispatcher.setReduceBandwidthMode(reduceBandwidthMode);
        eventDispatcher.setDatastreamIdsConfigured(currentConfiguration.values());

        currentConfiguration.forEach((conf, ids) -> {
            LOGGER.debug("Schedule of '{}' to dispatch starting in {} seconds and every {} seconds", ids,
                    conf.getSecondsFirstDispatch(), conf.getSecondsBetweenDispatches());
            ScheduledFuture dispatchTask =
                    executor.scheduleAtFixedRate(() -> scheduler.runFor(ids), conf.getSecondsFirstDispatch(),
                    conf.getSecondsBetweenDispatches(), TimeUnit.SECONDS);
            configuredTasks.add(dispatchTask);
        });
    }
}
