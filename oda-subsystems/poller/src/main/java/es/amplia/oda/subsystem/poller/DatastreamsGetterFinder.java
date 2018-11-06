package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DevicePattern;

import lombok.Data;

import java.util.List;
import java.util.Set;

public interface DatastreamsGetterFinder {
    @Data
    class Return
    {
        private final List<DatastreamsGetter> getters;
        private final Set<String> notFoundIds;
    }
    
    /**
     * This function will try to find the DatastreamsGetters that can generate values for the parameters specified.
     * Note that it is possible to return a set of DatastreamsGetters that, altogether, will generate more Datastreams
     * than the ones specified in the parameters.
     * @param deviceIdPattern The deviceIdPattern that the returned DatastreamsGetters must, at
     *  least partially, manage. Null or empty string means the ODA device itself. 
     * @param datastreamIdentifiers The Datastream identifiers that the returned DatastreamsGetters must generate. Not null.
     * @return A list with all the datastreams found that will generate values for the parameters specified, and a set
     *  of identifiers that no DatastreamsGetter manage.
     */
    Return getGettersSatisfying(DevicePattern deviceIdPattern, Set<String> datastreamIdentifiers);
}
