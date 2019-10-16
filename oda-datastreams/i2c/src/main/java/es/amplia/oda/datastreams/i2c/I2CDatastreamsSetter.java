package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class I2CDatastreamsSetter implements DatastreamsSetter {

	private final String datastreamId;
	private final I2CService service;
	private final Executor executor;

	I2CDatastreamsSetter(String datastreamId, I2CService service, Executor executor) {
		this.datastreamId = datastreamId;
		this.service = service;
		this.executor = executor;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return datastreamId;
	}

	@Override
	public Type getDatastreamType() {
		return ByteBuffer.class;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<Void> set(String deviceId, Object value) {
		return CompletableFuture.supplyAsync(() -> setValue(deviceId, value), executor);
	}

	private Void setValue(String deviceId, Object value) {
		try {
			I2CDevice device = service.getI2CFromName(datastreamId);
			ByteBuffer buffer = (ByteBuffer) value;
			device.write(buffer);
		} catch (I2CDeviceException | ClassCastException e) {
			throw new DataNotFoundException(createErrorMessage(deviceId, e.getMessage()));
		}

		return null;
	}

	private String createErrorMessage(String device, String description) {
		return String.format("Error setting %s value for %s device: %s", datastreamId, device, description);
	}
}
