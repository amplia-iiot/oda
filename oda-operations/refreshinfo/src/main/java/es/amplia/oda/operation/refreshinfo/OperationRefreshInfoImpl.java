package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationRefreshInfoImpl implements OperationRefreshInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationRefreshInfoImpl.class);


    private final DatastreamsGettersLocator datastreamsGettersLocator;


    OperationRefreshInfoImpl(DatastreamsGettersLocator datastreamsGettersLocator) {
        this.datastreamsGettersLocator = datastreamsGettersLocator;
    }
    
    @Value
    private static class CollectedValueWithIdAndError
    {
        private String datastreamId;
        private CollectedValue collectedValue;
        private Throwable error;
    }
    
    @Override
    public CompletableFuture<Result> refreshInfo(String deviceId) {
        List<DatastreamsGetter> getters = datastreamsGettersLocator.getDatastreamsGetters();
        
        List<CompletableFuture<CollectedValueWithIdAndError>> listOfFuturesValues = getters.stream()
            .filter(g->g.getDevicesIdManaged().contains(deviceId))
            .map(g-> processGetResult(deviceId, g))
            .collect(Collectors.toList());
        
        return allOf(listOfFuturesValues).thenApply(values->{
            Map<String, Object> obtained = values.stream()
                    .peek(this::logError)
                    .filter(v->v.error==null)
                    .collect(Collectors.toMap(CollectedValueWithIdAndError::getDatastreamId, v->v.getCollectedValue().getValue()));
            return new Result(obtained);
        });
    }

    private void logError(CollectedValueWithIdAndError collectedValue) {
        if (collectedValue.getError() != null) {
            LOGGER.error("Error getting value of {}: {}", collectedValue.getDatastreamId(), collectedValue.getError());
        }
    }

    private CompletableFuture<CollectedValueWithIdAndError> processGetResult(String deviceId, DatastreamsGetter g) {
        return Optional.ofNullable(g.get(deviceId))
                .map(f -> f.handle((cv,error)-> new CollectedValueWithIdAndError(g.getDatastreamIdSatisfied(), cv, error)))
                .orElse(createCollectedValueWithError(g.getDatastreamIdSatisfied()));
    }

    private CompletableFuture<CollectedValueWithIdAndError> createCollectedValueWithError(String datastreamId) {
        return CompletableFuture.completedFuture(
                new CollectedValueWithIdAndError(datastreamId, null, new RuntimeException("Error getting data")));
    }

    private static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return allDoneFuture.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );
    }

}
