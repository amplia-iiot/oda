package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;

import java.util.Set;

public interface Poller {
	void runFor(DevicePattern deviceIdPattern, Set<String> ids);
}
