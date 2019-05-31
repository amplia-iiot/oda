package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.operation.api.OperationSetClock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

class OperationSetClockImpl implements OperationSetClock {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationSetClockImpl.class);

	static final String CLOCK_DATASTREAM = "clock";


	private final DatastreamsSettersFinder datastreamsSettersFinder;


	OperationSetClockImpl(DatastreamsSettersFinder datastreamsSettersFinder) {
		this.datastreamsSettersFinder = datastreamsSettersFinder;
	}

	@Override
	public CompletableFuture<Result> setClock(String deviceId, long timestamp) {
		LOGGER.info("Set clock for device '{}'", deviceId);

		DatastreamsSettersFinder.Return setters =
				datastreamsSettersFinder.getSettersSatisfying(deviceId, Collections.singleton(CLOCK_DATASTREAM));

		DatastreamsSetter clockSetter = setters.getSetters().get(CLOCK_DATASTREAM);
		if (clockSetter == null) {
			return CompletableFuture
					.completedFuture(new Result(ResultCode.ERROR_PROCESSING, CLOCK_DATASTREAM + " can not be set"));
		}

		return clockSetter.set(deviceId, timestamp)
				.handle((ok, error) -> {
					if (error != null) {
						return new Result(ResultCode.ERROR_PROCESSING, "Error setting clock");
					}
					return new Result(ResultCode.SUCCESSFUL, null);
				});
	}
}
