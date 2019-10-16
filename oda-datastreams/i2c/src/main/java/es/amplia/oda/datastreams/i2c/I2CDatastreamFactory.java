package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;

import java.util.concurrent.Executor;

public class I2CDatastreamFactory {

	private I2CDatastreamFactory() {}

	static I2CDatastreamsGetter createDatastreamsGetter(String name, I2CService service, Executor executor) {
		return new I2CDatastreamsGetter(name, service, executor);
	}

	static I2CDatastreamsSetter createDatastreamsSetter(String name, I2CService service, Executor executor) {
		return new I2CDatastreamsSetter(name, service, executor);
	}
}
