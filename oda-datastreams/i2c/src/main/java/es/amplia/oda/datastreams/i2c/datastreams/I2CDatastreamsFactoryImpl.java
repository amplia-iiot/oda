package es.amplia.oda.datastreams.i2c.datastreams;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.datastreams.i2c.I2CDatastreamsFactory;

public class I2CDatastreamsFactoryImpl implements I2CDatastreamsFactory {

	private final I2CService service;


	public I2CDatastreamsFactoryImpl(I2CService service) {
		this.service = service;
	}

	@Override
	public DatastreamsGetter createDatastreamsGetter(String name, String device, long min, long max) {
		return new I2CDatastreamsGetter(name, device, min, max, service);
	}

	@Override
	public DatastreamsSetter createDatastreamsSetter(String name) {
		return new I2CDatastreamsSetter(name, service);
	}
}
