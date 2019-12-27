package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import lombok.Data;

import java.util.Map;
import java.util.Set;

public interface DatastreamsSettersFinder extends AutoCloseable {
    @Data
    class Return
    {
        private final Map<String, DatastreamsSetter> setters; //Map of identifiers to setters
        private final Set<String> notFoundIds;
    }

    /**
     * This function will try to find the DatastreamsSetters that can generate values for the parameters specified.
     *
     * @param deviceId The deviceId that the returned DatastreamsSetters must manage. Empty string means the ODA device itself.
     * @param datastreamIdentifiers The Datastream identifiers that the returned DatastreamsSetters must generate. Not null.
     * @return A list with all the datastreams found that will generate values for the parameters specified, and a set
     *  of identifiers that no DatastreamsSetter manage.
     */
    Return getSettersSatisfying(String deviceId, Set<String> datastreamIdentifiers);

    @Override
    void close();
}
