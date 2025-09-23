package es.amplia.oda.hardware.snmp.internal;

import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.VariableBinding;

import java.util.Vector;

@Slf4j
public class SnmpTrapProcessor implements CommandResponder {

    private final String deviceId;

    public SnmpTrapProcessor(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        log.info("Received Snmp Trap : {}", event);
        PDU pduReceived = event.getPDU();
        log.info("Received PDU : {}", pduReceived);

        // TODO : check error

        Vector<? extends VariableBinding> valuesReceived = pduReceived.getVariableBindings();
        for (VariableBinding var : valuesReceived) {
            log.info("Received value {} in OID {} from device {}", var.getVariable().toString(),
                    var.getOid().toString(), this.deviceId);
        }
    }
}
