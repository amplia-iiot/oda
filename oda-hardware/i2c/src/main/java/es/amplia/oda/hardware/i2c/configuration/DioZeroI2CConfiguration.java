package es.amplia.oda.hardware.i2c.configuration;

import lombok.Value;
import lombok.Builder;

@Value
@Builder
class DioZeroI2CConfiguration {
	String name;
	int controller;
	int address;
	int register;
	long min;
	long max;
}
