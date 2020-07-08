package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

class PollerConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollerConfigurationUpdateHandler.class);

    private final Poller poller;
    private final Scheduler scheduler;
    private final Map<PollConfiguration, Set<String>> currentConfiguration = new HashMap<>();


    PollerConfigurationUpdateHandler(Poller poller, Scheduler scheduler) {
        this.poller = poller;
        this.scheduler = scheduler;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Collection subsystem updated with {} properties", props.size());
        currentConfiguration.clear();

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
                add(new PollConfiguration(deviceIdInKey, firstPoll, secondsBetweenPolls), idInKey);
            } catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                LOGGER.error("Rejecting configuration '{}={}' because is invalid: {}", key, value, ex);
            }
        }
    }

    private void add(PollConfiguration key, String id) {
        Set<String> datastreams = currentConfiguration.computeIfAbsent(key, k -> new HashSet<>());
        datastreams.add(id);
    }

    @Override
    public void loadDefaultConfiguration() {
        currentConfiguration.clear();
    }

    @Override
    public void applyConfiguration() {
        scheduler.clear();
        currentConfiguration.forEach(this::schedulePoll);
    }

    private void schedulePoll(PollConfiguration pollConfiguration, Set<String> datastreamIds) {
        scheduler.schedule(() -> poller.poll(pollConfiguration.getDeviceIdPattern(), datastreamIds),
                pollConfiguration.getSecondsFirstPoll(), pollConfiguration.getSecondsBetweenPolls(), TimeUnit.SECONDS);
    }
}
