package es.amplia.oda.dispatcher.opengate;

import lombok.NonNull;
import lombok.Value;

@Value
class DispatchConfiguration {
    @NonNull
    private long secondsFirstDispatch;
    @NonNull
    private long secondsBetweenDispatches;
}
