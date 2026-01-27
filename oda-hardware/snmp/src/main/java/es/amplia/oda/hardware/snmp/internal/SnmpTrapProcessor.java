package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.hardware.snmp.SnmpCounters;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.VariableBinding;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Slf4j
public class SnmpTrapProcessor implements CommandResponder {

    private final SnmpTranslator translator;
    private final StateManagerProxy stateManager;
    // Map < IpAddress, DeviceId>
    Map<String, String> devicesIps;

    public SnmpTrapProcessor(SnmpTranslator translator, StateManagerProxy stateManager, Map<String, String> devicesIps) {
        this.translator = translator;
        this.stateManager = stateManager;
        this.devicesIps = devicesIps;
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        String ipAddress = event.getPeerAddress().toString().split("/")[0];
        log.debug("Received Snmp Event from address {} : {}", ipAddress, event);

        // get deviceId
        String deviceId = devicesIps.get(ipAddress);

        // incr counter
        SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_RECEIVED_EVENT, deviceId != null ? deviceId : ipAddress, 1);

        // get PDU
        PDU pduReceived = event.getPDU();
        int pduType = pduReceived.getType();

        // check error
        int pduErrorStatus = pduReceived.getErrorStatus();
        if (pduErrorStatus != 0) {
            log.warn("Pdu with error status {}", pduErrorStatus);
            return;
        }

        log.debug("Received PDU of type {}", PDU.getTypeString(pduType));
        // parse variables in PDU
        parseVariables(pduReceived, deviceId);
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

    private void parseVariables(PDU pdu, String deviceId) {
        Vector<? extends VariableBinding> valuesReceived = pdu.getVariableBindings();
        if (valuesReceived == null) {
            return;
        }

        if(deviceId == null) {
            log.warn("Can't parse data in PDU because there is no deviceId associated to that ip address");
            return;
        }

        // all events from the same pdu will have the same date
        long pduAt = System.currentTimeMillis();

        for (VariableBinding var : valuesReceived) {
            String OID = var.getOid().toString();
            String value = var.getVariable().toString();
            String varType = var.getVariable().getSyntaxString();

            // get translation
            SnmpEntry translation = translator.translate(OID, deviceId);
            if (translation == null) {
                continue;
            }

            log.debug("OID '{}', Type '{}', Value '{}'", OID, varType, value);
            log.debug("Value translation : {}", translation);

            value = value.trim();
            if (value.isEmpty()) {
                log.debug("Value retrieved is ignored because it is empty");
                continue;
            }

            // parse snmp entries to events
            List<Event> eventsToPublish = Collections.singletonList(new Event(translation.getDatastreamId(),
                    translation.getDeviceId(), null, translation.getFeed(), pduAt, value));

            // publish values
            if (translation.getEventPublishType().equalsIgnoreCase(SnmpEntry.EVENT_PUBLISH_TYPE_DISPATCHER)) {
                stateManager.publishValues(eventsToPublish);
            } else if (translation.getEventPublishType().equalsIgnoreCase(SnmpEntry.EVENT_PUBLISH_TYPE_STATEMANAGER)) {
                stateManager.onReceivedEvents(eventsToPublish);
            } else {
                log.error("Publish type '{}' for datastreamId '{}' and deviceId '{}' not supported",
                        translation.getEventPublishType(), translation.getDatastreamId(), translation.getDeviceId());
            }
        }
    }
}
