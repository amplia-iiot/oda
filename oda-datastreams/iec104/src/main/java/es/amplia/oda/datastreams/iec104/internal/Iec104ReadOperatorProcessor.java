package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.Iec104CacheValue;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import org.eclipse.neoscada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.neoscada.protocol.iec60870.asdu.message.InterrogationCommand;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.ASDUAddress;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.CauseOfTransmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

class Iec104ReadOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ReadOperatorProcessor.class);

    private final ScadaTableTranslator translator;
    private final Iec104ConnectionsFactory connectionsFactory;
    private Timer timer = null;

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

    public void updateGetterPolling(int initialPolling, int polling) {
        LOGGER.info("Update polling time. Initial delay {}, next executions every {} milliseconds", initialPolling, polling);
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer("IEC104 InterrogationCommand");

        TimerTask taskWithExceptionCatching = new TimerTask() {
            public void run() {
                try {
                    for (String deviceId : connectionsFactory.getConnectionsDeviceList()) {
                        Iec104ClientModule client = connectionsFactory.getConnection(deviceId);
                        if (client == null) {
                            continue;
                        }

                        Integer commonAddress = connectionsFactory.getCommonAddress(deviceId);
                        if (commonAddress == null) {
                            continue;
                        }

                        InterrogationCommand cmd = new InterrogationCommand(new ASDUHeader(CauseOfTransmission.ACTIVATED,
                                ASDUAddress.valueOf(commonAddress)), (short) 20);
                        if (client.isConnected()) {
                            LOGGER.info("Sending InterrogationCommand for device {}", deviceId);
                            client.send(cmd);
                        } else {
                            LOGGER.warn("Could not send InterrogationCommand due to no client connected for device {}", deviceId);
                        }
                    }
                } catch (Throwable t) {  // Catch Throwable rather than Exception (a subclass).
                    LOGGER.error("Caught exception in IEC104 TimerTask. StackTrace: ", t);
                }
            }
        };

        timer.schedule(taskWithExceptionCatching, initialPolling, polling);
    }

}
