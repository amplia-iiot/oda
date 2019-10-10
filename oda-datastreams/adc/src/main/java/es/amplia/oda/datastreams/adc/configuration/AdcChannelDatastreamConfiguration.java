package es.amplia.oda.datastreams.adc.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
class AdcChannelDatastreamConfiguration {
	private int channelPin;
	private String pinType;
	@NonNull
	private String datastreamId;
	private boolean getter;
	private boolean event;
}
