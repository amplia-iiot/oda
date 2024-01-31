package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.Iec104CacheValue;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

class Iec104ReadOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ReadOperatorProcessor.class);

    private final ScadaTableTranslator translator;
    private final Iec104ConnectionsFactory connectionsFactory;

    Iec104ReadOperatorProcessor(ScadaTableTranslator scadaTranslator, Iec104ConnectionsFactory connectionsFactory) {
        this.translator = scadaTranslator;
        this.connectionsFactory = connectionsFactory;
    }

    CollectedValue read(String deviceId, String datastreamId) {
        Iec104Cache ret = this.connectionsFactory.getCache(deviceId);

        if (ret == null) {
            return null;
        }

        // signals read from the cache are not events
        boolean isEvent = false;

        // get ASDU and address assigned to datastreamId
        ScadaInfo info = translator.translate(new DatastreamInfo(deviceId, datastreamId), isEvent);

        if (info == null) {
            return null;
        }

        //LOGGER.info("Scada info for datastreamId {} - type {}, index {}", datastreamId, info.getType(), info.getIndex());
        Iec104CacheValue valueFromCache = ret.getValue(info.getType().toString(), info.getIndex());

        if (valueFromCache == null) {
            LOGGER.debug("No value retrieved from cache for datastreamId {} for deviceId {}", datastreamId, deviceId);
            return null;
        }

        // if value is not null and if it hasn't been processed already
        if (valueFromCache.getValue() != null && !valueFromCache.isProcessed()) {
            LOGGER.debug("Value returned {} for device {} and datastream {}", valueFromCache.getValue(), deviceId, datastreamId);
            // mark value as already processed to avoid generating an event with the same value more than once
            ret.markValueAsProcessed(info.getType().toString(), info.getIndex());
            // get scadaTranslation info to get feed
            ScadaTableTranslator.ScadaTranslationInfo scadaTranslation = translator.getTranslationInfo(info, isEvent);
            return new CollectedValue(valueFromCache.getValueTime(), valueFromCache.getValue(), null,
                    scadaTranslation != null ? scadaTranslation.getFeed(): null);
        } else {
            LOGGER.debug("Value from cache is null or has already been read");
        }
        return null;
    }
}
