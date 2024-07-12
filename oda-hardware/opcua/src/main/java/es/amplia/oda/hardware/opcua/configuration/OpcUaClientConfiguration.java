package es.amplia.oda.hardware.opcua.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class OpcUaClientConfiguration {

    @NonNull
    String url;
    @NonNull
    String deviceId;
    String datapointsSubscription;
    
}
