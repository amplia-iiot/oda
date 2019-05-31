package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.operation.api.OperationSynchronizeClock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

class OperationSynchronizeClockImpl implements OperationSynchronizeClock {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationSynchronizeClockImpl.class);

	static final String CLOCK_DATASTREAM = "clock";


	private final DatastreamsSettersFinder datastreamsSettersFinder;


	OperationSynchronizeClockImpl(DatastreamsSettersFinder datastreamsSettersFinder) {
		this.datastreamsSettersFinder = datastreamsSettersFinder;
	}

	@Override
	public CompletableFuture<Result> synchronizeClock(String deviceId, String source) {
		LOGGER.info("Synchronize clock with system time for device '{}'. Ignoring source {}", deviceId, source);

		DatastreamsSettersFinder.Return setters =
				datastreamsSettersFinder.getSettersSatisfying(deviceId, Collections.singleton(CLOCK_DATASTREAM));

		DatastreamsSetter clockSetter = setters.getSetters().get(CLOCK_DATASTREAM);
		if (clockSetter == null) {
			return CompletableFuture
					.completedFuture(new Result(ResultCode.ERROR_PROCESSING, CLOCK_DATASTREAM +
							" can not be synchronize"));
		}

		return clockSetter.set(deviceId, System.currentTimeMillis())
				.handle((ok, error) -> {
					if (error != null) {
						return new Result(ResultCode.ERROR_PROCESSING, "Error synchronizing clock");
					}
					return new Result(ResultCode.SUCCESSFUL, null);
				});
	}
}
