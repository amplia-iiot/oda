package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class Iec104DatastreamsSetter implements DatastreamsSetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104DatastreamsSetter.class);

    private final String datastreamId;
    private final List<String> deviceIds;
    private final Iec104WriteOperatorProcessor writeOperatorProcessor;


    Iec104DatastreamsSetter(String datastreamId, List<String> deviceIds, Iec104WriteOperatorProcessor writeOperatorProcessor) {
        this.datastreamId = datastreamId;
        this.deviceIds = deviceIds;
        this.writeOperatorProcessor = writeOperatorProcessor;
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
    public CompletableFuture<Void> set(String device, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            LOGGER.debug("Setting value {} to datastream {} of device {}", value, datastreamId, device);
            writeOperatorProcessor.write(device, this.datastreamId, value);
            return null;
        });
    }

    @Override
    public Type getDatastreamType() {
        return Object.class;
    }
}
