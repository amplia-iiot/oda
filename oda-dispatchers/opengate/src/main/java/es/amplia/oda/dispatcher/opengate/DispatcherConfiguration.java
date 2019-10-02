package es.amplia.oda.dispatcher.opengate;

import lombok.NonNull;
import lombok.Value;

@Value
class DispatcherConfiguration {
    @NonNull
    private long initialDelay;
    @NonNull
    private long period;
}
