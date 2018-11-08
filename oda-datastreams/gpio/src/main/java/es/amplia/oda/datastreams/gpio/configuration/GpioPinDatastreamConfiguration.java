package es.amplia.oda.datastreams.gpio.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
class GpioPinDatastreamConfiguration {
    private int pinIndex;
    @NonNull
    private String datastreamId;
    private boolean getter;
    private boolean setter;
    private boolean event;
}
