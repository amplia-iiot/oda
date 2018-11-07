package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DevicePattern;

import lombok.Data;

import java.util.List;
import java.util.Set;

interface DatastreamsGetterFinder {
    @Data
    class Return
    {
        private final List<DatastreamsGetter> getters;
        private final Set<String> notFoundIds;
    }

    Return getGettersSatisfying(DevicePattern deviceIdPattern, Set<String> datastreamIdentifiers);
}
