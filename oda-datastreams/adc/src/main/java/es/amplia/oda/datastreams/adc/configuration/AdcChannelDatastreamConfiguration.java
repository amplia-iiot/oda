package es.amplia.oda.datastreams.adc.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "AdcChannelDatastreamConfigurationBuilder")
class AdcChannelDatastreamConfiguration {
	public static final int DEFAULT_CHANNEL_PIN = -1;
	public static final boolean DEFAULT_GETTER = true;
	public static final boolean DEFAULT_EVENT = false;
	public static final double DEFAULT_MINIMUM = 0.;
	public static final double DEFAULT_MAXIMUM = 1.;

	private String datastreamId;
	private int channelPin;
	private boolean getter;
	private boolean event;
	private double min;
	private double max;

	static class AdcChannelDatastreamConfigurationBuilder {
		private String datastreamId;
		private int channelPin = DEFAULT_CHANNEL_PIN;
		private boolean getter = DEFAULT_GETTER;
		private boolean event = DEFAULT_EVENT;
		private double min = DEFAULT_MINIMUM;
		private double max = DEFAULT_MAXIMUM;

		AdcChannelDatastreamConfigurationBuilder datastreamId(String datastreamId) {
			this.datastreamId = datastreamId;
			return this;
		}

		AdcChannelDatastreamConfigurationBuilder channelPin(int channelPin) {
			this.channelPin = channelPin;
			return this;
		}

		AdcChannelDatastreamConfigurationBuilder getter(boolean getter) {
			this.getter = getter;
			return this;
		}

		AdcChannelDatastreamConfigurationBuilder event(boolean event) {
			this.event = event;
			return this;
		}

		AdcChannelDatastreamConfigurationBuilder min(double min) {
			this.min = min;
			return this;
		}

		AdcChannelDatastreamConfigurationBuilder max(double max) {
			this.max = max;
			return this;
		}

		AdcChannelDatastreamConfiguration build() {
			checkConfigurationParams();
			return new AdcChannelDatastreamConfiguration(datastreamId, channelPin, getter, event, min, max);
		}

		private void checkConfigurationParams() {
			if (datastreamId == null) {
				throw new IllegalArgumentException("DatastreamId can't be null");
			}
			if (channelPin == DEFAULT_CHANNEL_PIN) {
				throw new IllegalArgumentException("Specify a valid channel pin");
			}
			if (!getter && !event) {
				throw new IllegalArgumentException("Datastreams must be at least getter or event");
			}
			if (min >= max) {
				throw new IllegalArgumentException("The value range is invalid");
			}
		}

	}
}
