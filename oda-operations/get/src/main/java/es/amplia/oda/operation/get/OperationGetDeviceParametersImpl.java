package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationGetDeviceParametersImpl implements OperationGetDeviceParameters {
	private static final Logger logger = LoggerFactory.getLogger(OperationGetDeviceParametersImpl.class);

	private final DatastreamsGetterFinder datastreamsGetterFinder;
	
	OperationGetDeviceParametersImpl(DatastreamsGetterFinder datastreamsGetterFinder) {
		this.datastreamsGetterFinder = datastreamsGetterFinder;
	}
	
	@Override
	public CompletableFuture<Result> getDeviceParameters(String deviceId, Set<String> dataStreamIds) {
		logger.debug("Getting values for device '{}': {}", deviceId, dataStreamIds);
		
		DatastreamsGetterFinder.Return finderReturn = datastreamsGetterFinder.getGettersSatisfying(deviceId, dataStreamIds);
		
		List<CompletableFuture<GetValue>> notFoundValues = getNotFoundIdsAsFutures(finderReturn.getNotFoundIds());
		List<CompletableFuture<GetValue>> allRecollectedValuesFutures = getFoundIdsAsFutures(deviceId, finderReturn.getGetters());
		allRecollectedValuesFutures.addAll(notFoundValues);
		
		CompletableFuture<Void> futureThatWillCompleteWhenAllFuturesComplete =
                CompletableFuture.allOf(allRecollectedValuesFutures.toArray(new CompletableFuture<?>[0]));

		CompletableFuture<Result> future = futureThatWillCompleteWhenAllFuturesComplete.thenApply(v -> {
				List<GetValue> values = allRecollectedValuesFutures.stream()
					.map(CompletableFuture::join)
					.collect(Collectors.toList());
				return new Result(values);
		});
		
		logger.debug("Wiring done. Waiting for all values to be complete.");
		return future;
	}

	private static List<CompletableFuture<GetValue>> getNotFoundIdsAsFutures(Set<String> notFoundIds) {
		return notFoundIds.stream()
			.map(id-> CompletableFuture.completedFuture(new GetValue(id, Status.NOT_FOUND, null, null)))
			.collect(Collectors.toList());
	}

	private static List<CompletableFuture<GetValue>> getFoundIdsAsFutures(String deviceId, List<DatastreamsGetter> getters) {
		return getters.stream()
                .map(dsp-> getValueFromFutureHandlingExceptions(deviceId, dsp))
                .collect(Collectors.toList());
	}

	private static CompletableFuture<GetValue> getValueFromFutureHandlingExceptions(String deviceId, DatastreamsGetter dsp) {
		String datastreamId = dsp.getDatastreamIdSatisfied();
		try {
			CompletableFuture<CollectedValue> getFuture = dsp.get(deviceId);
			return getFuture.handle((ok,error)-> {
				if (ok != null) {
					return new GetValue(datastreamId, Status.OK, ok.getValue(), null);
				} else {
					return new GetValue(datastreamId, Status.PROCESSING_ERROR, null, error.getMessage());
				}
			});
		} catch (Exception e) {
			return CompletableFuture.completedFuture(new GetValue(datastreamId, Status.PROCESSING_ERROR, null, e.getMessage()));
		}
	}
}
