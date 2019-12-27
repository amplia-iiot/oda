package es.amplia.oda.subsystem.collector.configuration;

import es.amplia.oda.core.commons.utils.DevicePattern;

import lombok.Value;

@Value
class CollectorConfiguration {
    private DevicePattern devicePattern;
    private String datastream;
    private long delay;
    private long period;
}
