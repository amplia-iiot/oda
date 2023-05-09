package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

class Iec104DatastreamsGetter implements DatastreamsGetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104DatastreamsGetter.class);

    private final String datastreamId;
    private final List<String> deviceIds;
    private final Iec104ReadOperatorProcessor readOperatorProcessor;


    Iec104DatastreamsGetter(String datastreamId, List<String> deviceIds, Iec104ReadOperatorProcessor readOperatorProcessor) {
        this.datastreamId = datastreamId;
        this.deviceIds = deviceIds;
        this.readOperatorProcessor = readOperatorProcessor;
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return deviceIds;
    }

    @Override
    public CompletableFuture<CollectedValue> get(String device) {
        LOGGER.debug("Getting value from the datastream {} of the the device {}", datastreamId, device);
        return CompletableFuture.supplyAsync(() ->
                readOperatorProcessor.read(device, datastreamId));
    }
}
