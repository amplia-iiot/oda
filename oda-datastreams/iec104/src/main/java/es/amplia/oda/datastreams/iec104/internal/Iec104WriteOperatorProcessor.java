package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Iec104WriteOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104WriteOperatorProcessor.class);

    private final Iec104ConnectionsFactory connectionsFactory;
    private final ScadaTableTranslator translator;

    Iec104WriteOperatorProcessor (ScadaTableTranslator scadaTranslator, Iec104ConnectionsFactory connectionsFactory) {
        this.translator = scadaTranslator;
        this.connectionsFactory = connectionsFactory;
    }

    void write(String deviceId, String datastreamId, Object value) {
        LOGGER.info("Setting value {} to device {}", value, deviceId);
        this.connectionsFactory.getConnection(deviceId).send(value);
    }
}
