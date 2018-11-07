package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DevicePattern;
import lombok.NonNull;
import lombok.Value;

@Value
class PollConfiguration {
    private long secondsFirstPoll;
    private long secondsBetweenPolls;
    @NonNull
    private DevicePattern deviceIdPattern;
}
