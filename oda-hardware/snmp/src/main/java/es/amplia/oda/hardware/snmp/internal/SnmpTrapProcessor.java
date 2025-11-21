package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.hardware.snmp.SnmpCounters;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.VariableBinding;

import java.util.Vector;

@Slf4j
public class SnmpTrapProcessor implements CommandResponder {

    private final SnmpTranslator translator;

    public SnmpTrapProcessor(SnmpTranslator translator) {
        this.translator = translator;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        // incr counter
        SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_RECEIVED_EVENT, null, 1);

        log.debug("Received Snmp Event from address {} : {}", event.getPeerAddress(), event);

        // get PDU
        PDU pduReceived = event.getPDU();
        int pduType = pduReceived.getType();

        // check error
        int pduErrorStatus = pduReceived.getErrorStatus();
        if (pduErrorStatus != 0) {
            log.warn("Pdu with error status {}", pduErrorStatus);
            return;
        }

        // process depending on the type
        if (pduType == PDU.V1TRAP) {
            processV1Trap((PDUv1) pduReceived);
        }
        else {
            log.debug("Received PDU of type {}", PDU.getTypeString(pduType));
            // get variables in PDU
            getVariables(pduReceived);
        }
    }

    private void processV1Trap(PDUv1 pduV1Trap) {
        int genericTrapType = pduV1Trap.getGenericTrap();
        log.info("Received V1TRAP of type {}", getV1TrapType(genericTrapType));

        // if it is enterprise specific
        if (genericTrapType == PDUv1.ENTERPRISE_SPECIFIC) {
            String enterpriseSpecificOID = pduV1Trap.getEnterprise().toString();
            int specificTrapType = pduV1Trap.getSpecificTrap();

            log.info("Specific type {}, Enterprise specific OID {}", specificTrapType, enterpriseSpecificOID);
            // TODO : process enterprise specific trap

        }
    }

    private String getV1TrapType(int trapType) {
        switch (trapType) {
            case PDUv1.COLDSTART:
                return "COLDSTART";
            case PDUv1.WARMSTART:
                return "WARMSTART";
            case PDUv1.LINKDOWN:
                return "LINKDOWN";
            case PDUv1.LINKUP:
                return "LINKUP";
            case PDUv1.AUTHENTICATIONFAILURE:
                return "AUTHENTICATIONFAILURE";
            case PDUv1.ENTERPRISE_SPECIFIC:
                return "ENTERPRISE_SPECIFIC";
            default:
                return String.valueOf(trapType);
        }
    }

    private void getVariables(PDU pdu) {
        Vector<? extends VariableBinding> valuesReceived = pdu.getVariableBindings();
        if (valuesReceived == null) {
            return;
        }

        log.debug("Variables in PDU : ");

        for (VariableBinding var : valuesReceived) {
            String OID = var.getOid().toString();
            String value = var.getVariable().toString();
            String varType = var.getVariable().getSyntaxString();
            log.debug("OID {}, Type {}, Value {}", OID, varType, value);
           /* SnmpEntry translation = translator.translate(OID, this.deviceId);
            log.info("Value translation : {}", translation);*/
        }
    }
}
