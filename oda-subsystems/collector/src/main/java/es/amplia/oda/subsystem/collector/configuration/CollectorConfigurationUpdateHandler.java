package es.amplia.oda.subsystem.collector.configuration;

import es.amplia.oda.subsystem.collector.Collector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Scheduler;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.*;

public class CollectorConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectorConfigurationUpdateHandler.class);


    private final Collector collector;
    private final Scheduler scheduler;
    private final List<CollectorConfiguration> currentConfiguration = new ArrayList<>();


    public CollectorConfigurationUpdateHandler(Collector collector, Scheduler scheduler) {
        this.collector = collector;
        this.scheduler = scheduler;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration for collector bundle");
        scheduler.clear();
        currentConfiguration.clear();

        for (Enumeration<String> elem = props.keys(); elem.hasMoreElements(); ) {
            String key = elem.nextElement();
            try {
                String[] keys = key.split(";");
                String datastreamId = keys[0];
                DevicePattern devicePattern = DevicePattern.NullDevicePattern;
                if (keys.length == 2) {
                    devicePattern = new DevicePattern(keys[1]);
                }
                String value = (String) props.get(key);
                String[] values = value.split(";");
                long delay;
                long period;
                if (values.length == 1) {
                    period = delay = Long.parseLong(values[0]);
                } else {
                    delay = Long.parseLong(values[0]);
                    period = Long.parseLong(values[1]);
                }
                currentConfiguration.add(new CollectorConfiguration(devicePattern, datastreamId, delay, period));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Invalid configuration entry: {}={}", key, props.get(key), e);
            }
        }
    }

    @Override
    public void loadDefaultConfiguration() {
        scheduler.clear();
        currentConfiguration.clear();
    }

    @Value
    private static class GroupCollectorConfiguration {
        private DevicePattern devicePattern;
        private long delay;
        private long period;

        static GroupCollectorConfiguration from(CollectorConfiguration conf) {
            return new GroupCollectorConfiguration(conf.getDevicePattern(), conf.getDelay(), conf.getPeriod());
        }
    }

    @Override
    public void applyConfiguration() {
        Map<GroupCollectorConfiguration, Set<String>> groupConfiguration =
                currentConfiguration.stream().collect(
                        groupingBy(GroupCollectorConfiguration::from, mapping(CollectorConfiguration::getDatastream, toSet())));
        groupConfiguration.forEach(this::scheduleCollect);
    }

    private void scheduleCollect(GroupCollectorConfiguration conf, Set<String> datastreams) {
        scheduler.schedule(() ->
                collector.collect(conf.getDevicePattern(), datastreams), conf.getDelay(), conf.getPeriod(), TimeUnit.SECONDS);
    }
}
