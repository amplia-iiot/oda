package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;

import java.util.Set;

interface Poller {
    void runFor(DevicePattern deviceIdPattern, Set<String> ids);
}
