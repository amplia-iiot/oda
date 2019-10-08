package es.amplia.oda.datastreams.diozero.datastreams;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractDatastreamsSetter implements DatastreamsSetter {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDatastreamsSetter.class);

	private final String datastreamId;
	private final int pinIndex;
	private final Executor executor;


	protected AbstractDatastreamsSetter(String datastreamId, int pinIndex, Executor executor) {
		this.datastreamId = datastreamId;
		this.pinIndex = pinIndex;
		this.executor = executor;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return this.datastreamId;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<Void> set(String deviceId, Object value) {
		try {
			return CompletableFuture.supplyAsync(() -> setDatastreamValue(deviceId, pinIndex, value), executor);
		} catch (ClassCastException e) {
			String errorMessage = createErrorMessage(deviceId, "Data stream value is not valid");
			throw new DataNotFoundException(errorMessage);
		}
	}

	private String createErrorMessage(String deviceId, String description) {
		String errorMessage = String.format("Error setting %s value for %s device: %s", datastreamId, deviceId, description);
		logger.warn(errorMessage);
		return errorMessage;
	}

	abstract Void setDatastreamValue(String deviceId, int pinIndex, Object value);
}
