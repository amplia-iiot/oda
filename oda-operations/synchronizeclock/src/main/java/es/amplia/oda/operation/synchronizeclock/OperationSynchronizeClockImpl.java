package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.statemanager.api.StateManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

class OperationSynchronizeClockImpl implements OperationSynchronizeClock {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationSynchronizeClockImpl.class);

	static final String CLOCK_DATASTREAM = "device.clock";


	private final StateManager stateManager;


	OperationSynchronizeClockImpl(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	public CompletableFuture<Result> synchronizeClock(String deviceId, String source) {
		LOGGER.info("Synchronize clock with system time for device '{}'. Ignoring source {}", deviceId, source);

		return stateManager.setDatastreamValue(deviceId, CLOCK_DATASTREAM, System.currentTimeMillis())
				.thenApply(this::mapValueToResult);
	}

	private Result mapValueToResult(DatastreamValue datastreamValue) {
		return new Result(mapStatusToResultCode(datastreamValue.getStatus()), datastreamValue.getError());
	}

	private ResultCode mapStatusToResultCode(Status status) {
		return status.equals(Status.OK) ? ResultCode.SUCCESSFUL : ResultCode.ERROR_PROCESSING;
	}
}
