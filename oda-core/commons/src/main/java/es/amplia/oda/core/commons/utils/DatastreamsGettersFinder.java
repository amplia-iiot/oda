package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import lombok.Data;

import java.util.List;
import java.util.Set;

public interface DatastreamsGettersFinder extends AutoCloseable {
    @Data
    class Return
    {
        private final List<DatastreamsGetter> getters;
        private final Set<String> notFoundIds;
    }

    Return getGettersSatisfying(DevicePattern deviceIdPattern, Set<String> datastreamIdentifiers);

    List<DatastreamsGetter> getGettersOfDevice(String deviceId);

    @Override
    void close();
}
