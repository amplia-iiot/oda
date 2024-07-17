package es.amplia.oda.datastreams.opcua.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.opcua.OpcUaConnection;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class OpcUaReadOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaReadOperatorProcessor.class);

    private final ScadaTableTranslator translator;
    private final OpcUaConnection connection;

    OpcUaReadOperatorProcessor(ScadaTableTranslator scadaTranslator, OpcUaConnection connection) {
        this.translator = scadaTranslator;
        this.connection = connection;
    }

    CollectedValue read(String deviceId, String datastreamId) {
        // signals read from the cache are not events
        boolean isEvent = false;

        // get ASDU and address assigned to datastreamId
        ScadaInfo info = translator.translate(new DatastreamInfo(deviceId, datastreamId), isEvent);

        if (info == null) {
            return null;
        }

        //LOGGER.info("Scada info for datastreamId {} - type {}, index {}", datastreamId, info.getType(), info.getIndex());
        try {
            Object value = connection.readVariable(Integer.toString(info.getIndex()));

            LOGGER.debug("Value returned {} for device {} and datastream {}", value, deviceId, datastreamId);
            // get scadaTranslation info to get feed
            ScadaTableTranslator.ScadaTranslationInfo scadaTranslation = translator.getTranslationInfo(info, isEvent);
            return new CollectedValue(System.currentTimeMillis(), value, null,
                    scadaTranslation != null ? scadaTranslation.getFeed(): null);
        } catch (Exception e) {
            LOGGER.error("Error reading value", e);
        }
        return null;
    }
}
