package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class OperationSynchronizeClockImpl implements OperationSynchronizeClock {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationSynchronizeClockImpl.class);

	private String clockDatastream = "device.clock";


	private final StateManager stateManager;


	OperationSynchronizeClockImpl(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	public CompletableFuture<Result> synchronizeClock(String deviceId, String source) {
		LOGGER.debug("Synchronize clock with system time for device '{}'. Ignoring source {}", deviceId, source);

		return stateManager.setDatastreamValue(deviceId, clockDatastream, System.currentTimeMillis())
				.thenApply(this::mapValueToResult);
	}

	private Result mapValueToResult(DatastreamValue datastreamValue) {
		return new Result(mapStatusToResultCode(datastreamValue.getStatus()), datastreamValue.getError());
	}

	private ResultCode mapStatusToResultCode(Status status) {
		return status.equals(Status.OK) ? ResultCode.SUCCESSFUL : ResultCode.ERROR_PROCESSING;
	}

	public void loadConfiguration(String clockDatastream) {
		this.clockDatastream = clockDatastream;
	}
}
