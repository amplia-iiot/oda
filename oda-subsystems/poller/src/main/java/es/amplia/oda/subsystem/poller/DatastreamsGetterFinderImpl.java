package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DevicePattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DatastreamsGetterFinderImpl implements DatastreamsGetterFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamsGetterFinderImpl.class);

    private final DatastreamsGettersLocator datastreamsGettersLocator;

    DatastreamsGetterFinderImpl(DatastreamsGettersLocator datastreamsGettersLocator) {
        this.datastreamsGettersLocator = datastreamsGettersLocator;
    }

    @Override
    public Return getGettersSatisfying(DevicePattern deviceIdPattern, Set<String> datastreamIdentifiers) {
        if (deviceIdPattern == null)
            throw new IllegalArgumentException("DevicePattern for getGettersSatisfying must be not null");
        try {
            final Set<String> notFoundIds = new HashSet<>(datastreamIdentifiers);
            List<DatastreamsGetter> providers = datastreamsGettersLocator.getDatastreamsGetters().stream().
                    filter(dsp-> !Collections.disjoint(Collections.singleton(dsp.getDatastreamIdSatisfied()),
                                     datastreamIdentifiers)).
                    filter(dsp-> dsp.getDevicesIdManaged().stream().anyMatch(deviceIdPattern::match)).
                    peek(dsp-> notFoundIds.remove(dsp.getDatastreamIdSatisfied())).
                    collect(Collectors.toList());
            return new Return(providers, notFoundIds);
        } catch (Exception e) {
            LOGGER.error("Exception when trying to determine providers satisfying {}/{}: {}", deviceIdPattern,
                    datastreamIdentifiers, e);
            return new Return(Collections.emptyList(), new HashSet<>(datastreamIdentifiers));
        }
    }
}
