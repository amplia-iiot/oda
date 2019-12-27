package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;

public class DatastreamsGettersFinderImpl implements DatastreamsGettersFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamsGettersFinderImpl.class);


    private final ServiceLocator<DatastreamsGetter> datastreamsGettersLocator;


    public DatastreamsGettersFinderImpl(ServiceLocator<DatastreamsGetter> datastreamsGettersLocator) {
        this.datastreamsGettersLocator = datastreamsGettersLocator;
    }

    @Override
    public Return getGettersSatisfying(DevicePattern deviceIdPattern, Set<String> datastreamIdentifiers) {
        if (Objects.isNull(deviceIdPattern)) {
            throw new IllegalArgumentException("DevicePattern for getGettersSatisfying must be not null");
        }

        try {
            List<DatastreamsGetter> providers = datastreamsGettersLocator.findAll().stream()
                    .filter(dsp-> datastreamIdentifiers.contains(dsp.getDatastreamIdSatisfied()))
                    .filter(dsp-> dsp.getDevicesIdManaged().stream().anyMatch(deviceIdPattern::match))
                    .collect(Collectors.toList());
            Set<String> notFoundIds = new HashSet<>(datastreamIdentifiers);
            notFoundIds.removeAll(
                    providers.stream()
                            .map(DatastreamsGetter::getDatastreamIdSatisfied)
                            .collect(Collectors.toList()));
            return new Return(providers, notFoundIds);
        } catch (Exception e) {
            LOGGER.error("Exception when trying to determine providers satisfying {}/{}: {}", deviceIdPattern,
                    datastreamIdentifiers, e);
            return new Return(Collections.emptyList(), new HashSet<>(datastreamIdentifiers));
        }
    }

    @Override
    public List<DatastreamsGetter> getGettersOfDevice(String deviceId) {
        return datastreamsGettersLocator.findAll().stream()
                .filter(datastreamsGetter -> datastreamsGetter.getDevicesIdManaged().contains(deviceId))
                .collect(Collectors.toList());
    }

    @Override
    public void close() {
        datastreamsGettersLocator.close();
    }
}
