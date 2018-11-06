package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DevicePattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DatastreamsGetterFinderImpl implements DatastreamsGetterFinder {
    private static final Logger logger = LoggerFactory.getLogger(DatastreamsGetterFinderImpl.class);

    private DatastreamsGettersLocator datastreamsGettersLocator;

    DatastreamsGetterFinderImpl(DatastreamsGettersLocator datastreamsGettersLocator) {
        this.datastreamsGettersLocator = datastreamsGettersLocator;
    }
    
    /**
     * This function will try to find the DatastreamsGetters that can generate values for the parameters specified.
     * Note that it is possible to return a set of DatastreamsGetters that, altogether, will generate more Datastreams
     * than the ones specified in the parameters.
     * @param deviceIdPattern The deviceIdPattern that the returned DatastreamsGetters must, at
     *  least partially, manage. Must be not null. 
     * @param datastreamIdentifiers The Datastream identifiers that the returned DatastreamsGetters must generate. Not null.
     * @return A list with all the datastreams found that will generate values for the parameters specified, and a set
     *  of identifiers that no DatastreamsGetter manage.
     */
    @Override
    public Return getGettersSatisfying(DevicePattern deviceIdPattern, Set<String> datastreamIdentifiers) {
        if (deviceIdPattern == null)
            throw new IllegalArgumentException("DevicePattern for getGettersSatisfying must be not null");
        try {
            final Set<String> notFoundIds = new HashSet<>(datastreamIdentifiers);
            List<DatastreamsGetter> providers = datastreamsGettersLocator.getDatastreamsGetters().stream().
                    filter(dsp-> !Collections.disjoint(Collections.singleton(dsp.getDatastreamIdSatisfied()),datastreamIdentifiers)).
                    filter(dsp-> dsp.getDevicesIdManaged().stream().anyMatch(deviceIdPattern::match)).
                    peek(dsp-> notFoundIds.remove(dsp.getDatastreamIdSatisfied())).
                    collect(Collectors.toList());
            return new Return(providers, notFoundIds);
        } catch (Exception e) {
            logger.error("Exception when trying to determine providers satisfiying {}/{}: {}", deviceIdPattern, datastreamIdentifiers, e);
            return new Return(Collections.emptyList(), new HashSet<>(datastreamIdentifiers));
        }
    }
}
