package es.amplia.oda.operation.setclock;

import es.amplia.oda.operation.api.OperationSetClock;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.statemanager.api.StateManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

class OperationSetClockImpl implements OperationSetClock {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationSetClockImpl.class);

	static final String CLOCK_DATASTREAM = "device.clock";


	private final StateManager stateManager;


	OperationSetClockImpl(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	public CompletableFuture<Result> setClock(String deviceId, long timestamp) {
		LOGGER.info("Set clock for device '{}'", deviceId);

		return stateManager.setDatastreamValue(deviceId, CLOCK_DATASTREAM, timestamp)
				.thenApply(this::mapDatastreamValueToResult);
	}

	private Result mapDatastreamValueToResult(DatastreamValue datastreamValue) {
		return new Result(mapStatusToResultCode(datastreamValue.getStatus()), datastreamValue.getError());
	}

	private ResultCode mapStatusToResultCode(Status status) {
		return status.equals(Status.OK) ? ResultCode.SUCCESSFUL : ResultCode.ERROR_PROCESSING;
	}
}
