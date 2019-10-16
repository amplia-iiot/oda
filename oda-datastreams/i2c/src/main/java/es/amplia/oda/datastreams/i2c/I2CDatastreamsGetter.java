package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class I2CDatastreamsGetter implements DatastreamsGetter {

	private final String datastreamId;
	private final I2CService service;
	private final Executor executor;

	I2CDatastreamsGetter(String datastreamId, I2CService service, Executor executor) {
		this.datastreamId = datastreamId;
		this.service = service;
		this.executor = executor;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return datastreamId;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<CollectedValue> get(String deviceId) {
		return CompletableFuture.supplyAsync(() -> getDatastreamIdValuesForDevicePattern(deviceId), executor);
	}

	private CollectedValue getDatastreamIdValuesForDevicePattern(String deviceId) {
		try {
			I2CDevice device = service.getI2CFromName(datastreamId);
			long at = System.currentTimeMillis();
			long value = device.readUInt();
			return new CollectedValue(at, value);
		} catch (I2CDeviceException e) {
			throw new DataNotFoundException(
					String.format("Error getting %s value for %s device: %s",
							datastreamId, deviceId, e.getMessage()));
		}
	}
}
