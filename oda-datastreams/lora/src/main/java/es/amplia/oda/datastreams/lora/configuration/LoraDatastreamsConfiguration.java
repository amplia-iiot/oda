package es.amplia.oda.datastreams.lora.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoraDatastreamsConfiguration {
	String deviceId;
}
