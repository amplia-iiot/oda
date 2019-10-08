package es.amplia.oda.datastreams.diozero.configuration;

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
	private boolean setter;
	private boolean event;
}
