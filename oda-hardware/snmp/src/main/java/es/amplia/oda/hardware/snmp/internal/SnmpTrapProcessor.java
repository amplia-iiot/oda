package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.hardware.snmp.SnmpCounters;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.VariableBinding;

import java.util.Vector;

@Slf4j
public class SnmpTrapProcessor implements CommandResponder {

    private final String deviceId;
    private final SnmpTranslator translator;

    public SnmpTrapProcessor(String deviceId, SnmpTranslator translator) {
        this.deviceId = deviceId;
        this.translator = translator;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        // incr counter
        SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_RECEIVED_EVENT, this.deviceId, 1);

        log.info("Received Snmp Trap : {}", event);
        PDU pduReceived = event.getPDU();
        log.info("Received PDU of type {} : {}", pduReceived.getType(), pduReceived);
        // TODO : check error

        Vector<? extends VariableBinding> valuesReceived = pduReceived.getVariableBindings();
        if (valuesReceived == null) {
            return;
        }

        for (VariableBinding var : valuesReceived) {
            String OID = var.getOid().toString();
            String value = var.getVariable().toString();
            String varType = var.getVariable().getSyntaxString();
            log.info("Received value of type {} in OID {} from device {} = {}", varType, OID, this.deviceId, value);
            SnmpEntry translation = translator.translate(OID, this.deviceId);
            log.info("Value translation : {}", translation);
        }
    }
}
