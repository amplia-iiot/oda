package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.DevicePattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class PollerConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollerConfigurationUpdateHandler.class);

    private final ScheduledExecutorService executor;
    private final Poller poller;
    private final Map<PollConfiguration, Set<String>> currentConfiguration = new HashMap<>();
    private final List<ScheduledFuture> configuredTasks = new ArrayList<>();

    PollerConfigurationUpdateHandler(ScheduledExecutorService executor, Poller poller) {
        this.executor = executor;
        this.poller = poller;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Collection subsystem updated with {} properties", props.size());
        Enumeration<String> keysEnumeration = props.keys();
        while(keysEnumeration.hasMoreElements()) {
            String key = keysEnumeration.nextElement();
            String value = (String) props.get(key);
            try {
                String[] keyFields = key.split("\\s*(;\\s*)");
                String idInKey;
                DevicePattern deviceIdInKey;
                if(keyFields.length == 1) {
                    idInKey = keyFields[0];
                    deviceIdInKey=DevicePattern.NullDevicePattern;
                } else {
                    idInKey = keyFields[0];
                    deviceIdInKey=new DevicePattern(keyFields[1]);
                }

                String[] valueFields = value.split("\\s*(;\\s*)");
                long firstPoll;
                long secondsBetweenPolls;
                if (valueFields.length == 1) {
                    firstPoll = Long.parseLong(valueFields[0]);
                    secondsBetweenPolls = firstPoll;
                } else {
                    firstPoll = Long.parseLong(valueFields[0]);
                    secondsBetweenPolls = Long.parseLong(valueFields[1]);
                }
                add(new PollConfiguration(firstPoll, secondsBetweenPolls, deviceIdInKey), idInKey);
            } catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                LOGGER.info("Rejecting configuration '{}={}' because is invalid: {}", key, value, ex);
            }
        }
    }

    private void add(PollConfiguration key, String id) {
        Set<String> datastreams = currentConfiguration.computeIfAbsent(key, k -> new HashSet<>());
        datastreams.add(id);
    }

    @Override
    public void applyConfiguration() {
        configuredTasks.forEach(task -> task.cancel(false));
        configuredTasks.clear();
        currentConfiguration.forEach((poll, ids) ->{
            LOGGER.debug("Poll of '{}' for deviceIdPattern '{}' starting in {} seconds and every {} seconds",
                    ids, poll.getDeviceIdPattern(), poll.getSecondsFirstPoll(), poll.getSecondsBetweenPolls());
            ScheduledFuture pollTask =
                    executor.scheduleWithFixedDelay(() ->
                            poller.runFor(poll.getDeviceIdPattern(), ids), poll.getSecondsFirstPoll(),
                                            poll.getSecondsBetweenPolls(), TimeUnit.SECONDS);
            configuredTasks.add(pollTask);
        });
    }
}
