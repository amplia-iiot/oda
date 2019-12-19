package es.amplia.oda.subsystem.collector;

import es.amplia.oda.core.commons.utils.DevicePattern;

import java.util.Set;

public interface Collector {
    void collect(DevicePattern devicePattern, Set<String> datastreams);
}
