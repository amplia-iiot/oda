package es.amplia.oda.datastreams.i2c.datastreams;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class I2CDatastreamsGetter implements DatastreamsGetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(I2CDatastreamsGetter.class);

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
			LOGGER.debug("Getting value {} from I2C datastream {} of device {} at {}", value, datastreamId, deviceId, at);
			return new CollectedValue(at, value);
		} catch (I2CDeviceException e) {
			String msg = String.format("Error getting %s value for %s device: %s", datastreamId, deviceId, e.getMessage());
			LOGGER.error(msg, e);
			throw new DataNotFoundException(msg);
		} catch (InterruptedException e) {
			String msg = String.format("Error getting %s value for %s device: %s", datastreamId, deviceId, e.getMessage());
			LOGGER.error(msg, e);
			Thread.currentThread().interrupt();
			throw new DataNotFoundException(msg);
		}
	}
}
