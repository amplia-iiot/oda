package es.amplia.oda.dispatcher.opengate;

import lombok.NonNull;
import lombok.Value;

@Value
class DispatcherConfiguration {
    @NonNull
    long initialDelay;
    @NonNull
    long period;
}
