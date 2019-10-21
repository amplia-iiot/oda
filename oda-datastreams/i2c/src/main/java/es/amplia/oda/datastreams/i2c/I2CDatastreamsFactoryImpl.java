package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

class I2CDatastreamsFactoryImpl implements I2CDatastreamsFactory {

	private final I2CService service;


	I2CDatastreamsFactoryImpl(I2CService service) {
		this.service = service;
	}

	@Override
	public DatastreamsGetter createDatastreamsGetter(String name, long min, long max) {
		return new I2CDatastreamsGetter(name, min, max, service);
	}

	@Override
	public DatastreamsSetter createDatastreamsSetter(String name) {
		return new I2CDatastreamsSetter(name, service);
	}
}
