package es.amplia.oda.datastreams.i2c.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderClassName = "I2CDatastreamsConfigurationBuilder")
class I2CDatastreamsConfiguration {
	public static final long DEFAULT_MIN = 0L;
	public static final long DEFAULT_MAX = 1L;
	public static final boolean DEFAULT_GETTER = true;
	public static final boolean DEFAULT_SETTER = false;

	private String name;
	private long min;
	private long max;
	private boolean getter;
	private boolean setter;

	static class I2CDatastreamsConfigurationBuilder {
		private String name;
		private long min = DEFAULT_MIN;
		private long max = DEFAULT_MAX;
		private boolean getter = DEFAULT_GETTER;
		private boolean setter = DEFAULT_SETTER;

		I2CDatastreamsConfigurationBuilder name(String name) {
			this.name = name;
			return this;
		}

		I2CDatastreamsConfigurationBuilder min(long min) {
			this.min = min;
			return this;
		}

		I2CDatastreamsConfigurationBuilder max(long max) {
			this.max = max;
			return this;
		}

		I2CDatastreamsConfigurationBuilder getter(boolean getter) {
			this.getter = getter;
			return this;
		}

		I2CDatastreamsConfigurationBuilder setter(boolean setter) {
			this.setter = setter;
			return this;
		}

		I2CDatastreamsConfiguration build() {
			checkConfigurationParams();
			return new I2CDatastreamsConfiguration(name, min, max, getter, setter);
		}

		private void checkConfigurationParams() {
			if (name == null || (!getter && ! setter) || (min >= max)) {
				throw new IllegalArgumentException("Invalid configuration for I2C Datastreams: name=" + name +
						", min=" + min + ", max=" + max + ", getter=" + getter + ", setter=" + setter);
			}
		}
	}
}
