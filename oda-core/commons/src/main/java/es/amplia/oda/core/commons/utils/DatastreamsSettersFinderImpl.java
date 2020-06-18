package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;

public class DatastreamsSettersFinderImpl implements DatastreamsSettersFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamsSettersFinderImpl.class);

    private final ServiceLocator<DatastreamsSetter> datastreamsSettersLocator;

    public DatastreamsSettersFinderImpl(ServiceLocator<DatastreamsSetter> datastreamsSettersLocator) {
        this.datastreamsSettersLocator = datastreamsSettersLocator;
    }

    @Override
    public DatastreamsSettersFinder.Return getSettersSatisfying(String deviceId, Set<String> datastreamIdentifiers) {
        if (Objects.isNull(deviceId)) {
            throw new IllegalArgumentException("DevicePattern for getGettersSatisfying must be not null");
        }

        try {
            Map<String, DatastreamsSetter> providers = datastreamsSettersLocator.findAll().stream()
                    .filter(dsp-> datastreamIdentifiers.contains(dsp.getDatastreamIdSatisfied()))
                    .filter(dsp-> dsp.getDevicesIdManaged().contains(deviceId))
                    .collect(Collectors.toMap(DatastreamsSetter::getDatastreamIdSatisfied, dsp -> dsp));
            Set<String> notFoundIds = new HashSet<>(datastreamIdentifiers);
            notFoundIds.removeAll(providers.keySet());
            return new DatastreamsSettersFinder.Return(providers, notFoundIds);
        } catch (Exception e) {
            LOGGER.error("Exception when trying to determine providers satisfying {}/{}: ", deviceId,
                    datastreamIdentifiers, e);
            return new DatastreamsSettersFinder.Return(Collections.emptyMap(), new HashSet<>(datastreamIdentifiers));
        }
    }

    @Override
    public void close() {
        datastreamsSettersLocator.close();
    }
}
