package es.amplia.oda.datastreams.opcua.internal;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.opcua.OpcUaConnection;
import es.amplia.oda.core.commons.utils.DatastreamInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OpcUaWriteOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaWriteOperatorProcessor.class);

    private final OpcUaConnection connection;
    private final ScadaTableTranslator translator;

    OpcUaWriteOperatorProcessor (ScadaTableTranslator scadaTranslator, OpcUaConnection connection) {
        this.translator = scadaTranslator;
        this.connection = connection;
    }

    void write(String deviceId, String datastreamId, Object value) {
        LOGGER.info("Setting value {} to device {}", value, deviceId);
        // get ASDU and address assigned to datastreamId
        ScadaInfo info = translator.translate(new DatastreamInfo(deviceId, datastreamId), false);

        if (info == null) {
            return;
        }

        //LOGGER.info("Scada info for datastreamId {} - type {}, index {}", datastreamId, info.getType(), info.getIndex());
        try {
            connection.writeVariable(Integer.toString(info.getIndex()), value);
        } catch (Exception e) {
            LOGGER.error("Error wirtting value", e);
        }
        return;
    }
}
