package es.amplia.oda.datastreams.i2c.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class I2CDatastreamConfiguration {
	String name;
	boolean getter;
	boolean setter;
}
