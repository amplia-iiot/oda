package es.amplia.oda.datastreams.i2c.datastreams;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class I2CDatastreamsGetter implements DatastreamsGetter {

	private final String datastreamId;
	private final String device;
	private final long min;
	private final long max;
	private final I2CService service;


	public I2CDatastreamsGetter(String datastreamId, String device, long min, long max, I2CService service) {
		this.datastreamId = datastreamId;
		this.device = device;
		this.service = service;
		this.min = min;
		this.max = max;
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
		return CompletableFuture.supplyAsync(() -> getDatastreamIdValuesForDevicePattern(deviceId));
	}

	private CollectedValue getDatastreamIdValuesForDevicePattern(String deviceId) {
		try {
			I2CDevice i2CDevice = service.getI2CFromName(device);
			long at = System.currentTimeMillis();
			double value = (i2CDevice.readScaledData() * (max-min)) + min;
			return new CollectedValue(at, value);
		} catch (I2CDeviceException | InterruptedException e) {
			throw new DataNotFoundException(
					String.format("Error getting %s value for %s device: %s",
							datastreamId, deviceId, e.getMessage()));
		}
	}
}
