package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

public class I2CDatastreamsRegistry {

	private final I2CDatastreamsFactory i2CDatastreamsFactory;
	private final ServiceRegistrationManager<DatastreamsGetter> getterRegistrationManager;
	private final ServiceRegistrationManager<DatastreamsSetter> setterRegistrationManager;


	I2CDatastreamsRegistry(I2CDatastreamsFactory i2CDatastreamsFactory,
						   ServiceRegistrationManager<DatastreamsGetter> getterRegistrationManager,
						   ServiceRegistrationManager<DatastreamsSetter> setterRegistrationManager) {
		this.i2CDatastreamsFactory = i2CDatastreamsFactory;
		this.getterRegistrationManager = getterRegistrationManager;
		this.setterRegistrationManager = setterRegistrationManager;
	}

	public void addDatastreamGetter(String name, String device, long min, long max) {
		DatastreamsGetter getter = i2CDatastreamsFactory.createDatastreamsGetter(name, device, min, max);
		getterRegistrationManager.register(getter);
	}

	public void addDatastreamSetter(String name) {
		DatastreamsSetter setter = i2CDatastreamsFactory.createDatastreamsSetter(name);
		setterRegistrationManager.register(setter);
	}

	public void close() {
		getterRegistrationManager.unregister();
		setterRegistrationManager.unregister();
	}
}
