package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationRefreshInfoImpl implements OperationRefreshInfo {

    private final DatastreamsGettersLocator datastreamsGettersLocator;

    OperationRefreshInfoImpl(DatastreamsGettersLocator datastreamsGettersLocator) {
        this.datastreamsGettersLocator = datastreamsGettersLocator;
    }
    
    @Value
    private class CollectedValueWithIdAndError
    {
        String datastreamId;
        CollectedValue collectedValue;
        Throwable error;
    }
    
    @Override
    public CompletableFuture<Result> refreshInfo(String deviceId) {
        List<DatastreamsGetter> getters = datastreamsGettersLocator.getDatastreamsGetters();
        
        List<CompletableFuture<CollectedValueWithIdAndError>> listOfFuturesValues = getters.stream()
            .filter(g->g.getDevicesIdManaged().contains(deviceId))
            .map(g-> g.get(deviceId).handle((cv,error)-> new CollectedValueWithIdAndError(g.getDatastreamIdSatisfied(), cv, error)))
            .collect(Collectors.toList());
        
        return allOf(listOfFuturesValues).thenApply(values->{
            Map<String, Object> obtained = values.stream()
                .filter(v->v.error==null)
                .collect(Collectors.toMap(CollectedValueWithIdAndError::getDatastreamId, v->v.getCollectedValue().getValue()));
            return new Result(obtained);
        });
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
