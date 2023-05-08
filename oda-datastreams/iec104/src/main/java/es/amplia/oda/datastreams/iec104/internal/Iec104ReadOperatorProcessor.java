package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.DatastreamInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
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
        if (ret != null) {
            ScadaInfo info = translator.translate(new DatastreamInfo(deviceId, datastreamId, null));
            Object value = ret.getValue(info.getType().toString(), info.getIndex());
            LOGGER.debug("Value returned {} for device {} and datastream {}", value, deviceId, datastreamId);
            if (value != null) return new CollectedValue(System.currentTimeMillis(), value);
        }
        return null;
    }

    public void updateGetterPolling(int polling) {
        LOGGER.info("Update polling time {}", polling);
        if (timer!= null) {
            timer.cancel();
        }
        timer = new Timer("IEC104 InterrogationCommand");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String deviceId: connectionsFactory.getDeviceList()) {
                    LOGGER.info("Sending InterrogationCommand for device {}", deviceId);
                    Iec104ClientModule client = connectionsFactory.getConnection(deviceId);
                    int commonAddress = connectionsFactory.getCommonAddress(deviceId);
                    InterrogationCommand cmd = new InterrogationCommand(new ASDUHeader(CauseOfTransmission.ACTIVATED, ASDUAddress.valueOf(commonAddress)), (short) 20);
                    if (client.isConnected()) client.send(cmd);
                    else LOGGER.warn("Could not send command due to no client connected for device {}", deviceId);
                }
            }
        }, polling, polling);
    }

}
