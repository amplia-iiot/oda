package es.amplia.oda.hardware.i2c.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public
class DioZeroI2CConfiguration {
	String name;
	int controller;
	int address;
	int register;
	long min;
	long max;
}
