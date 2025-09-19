package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.hardware.snmp.configuration.SnmpClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
public class SnmpRequests {

    public static PDU getRequest(SnmpClientConfig snmpClient, String OID) {
        log.info("Getting value in OID {}", OID);

        PDU pdu = null;
        int version = snmpClient.getVersion();
        if (version == 1 || version == 2) {
            pdu = getRequest(OID);
        } else if (version == 3) {
            pdu = getRequestV3(OID, snmpClient.getV3Options().getContextName());
        }
        return pdu;
    }

    public static PDU setRequest(SnmpClientConfig snmpClient, String OID, String newValue) {
        log.info("Setting value {} in OID {}", newValue, OID);

        PDU pdu = null;
        int version = snmpClient.getVersion();
        if (version == 1 || version == 2) {
            pdu = setRequest(OID, newValue);
        } else if (version == 3) {
            pdu = setRequestV3(OID, newValue, snmpClient.getV3Options().getContextName());
        }
        return pdu;
    }

    private static PDU getRequest(String OID){
        log.info("Getting value in OID {}", OID);
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.add(new VariableBinding(new OID(OID)));
        return pdu;
    }

    private static PDU getRequestV3(String OID, String contextName){
        log.info("Getting value in OID {}", OID);
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.GET);
        pdu.setContextName(new OctetString(contextName));
        pdu.add(new VariableBinding(new OID(OID)));
        return pdu;
    }

    private static PDU setRequest(String OID, String newValue) {
        log.info("Setting value {} in OID {}", newValue, OID);
        PDU pdu = new PDU();
        pdu.setType(PDU.SET);
        VariableBinding variableBind = new VariableBinding(new OID(OID), new OctetString(newValue));
        pdu.add(variableBind);
        return pdu;
    }

    private static PDU setRequestV3(String OID, String newValue, String contextName) {
        log.info("Setting value {} in OID {}", newValue, OID);
        ScopedPDU pdu = new ScopedPDU();
        pdu.setType(PDU.SET);
        pdu.setContextName(new OctetString(contextName));
        VariableBinding variableBind = new VariableBinding(new OID(OID), new OctetString(newValue));
        pdu.add(variableBind);
        return pdu;
    }

    public static List<VariableBinding> parseResponse(ResponseEvent event) throws Exception {
        List<VariableBinding> variableValues = new ArrayList<>();

        // get response
        PDU response = checkResponse(event);
        if (response == null) {
            return variableValues;
        }

        // retrieve values in response
        Vector<? extends VariableBinding> varBindings = event.getResponse().getVariableBindings();
        return new ArrayList<>(varBindings);
    }

    public static PDU checkResponse(ResponseEvent event) throws Exception {
        // check error
        Exception error = event.getError();
        if (error != null) {
            log.error("Error in response event : ", error);
            throw error;
        }

        // get response
        PDU response = event.getResponse();
        if (response == null) {
            log.warn("Response is null");
        }

        log.info("Response = {}", response);
        return response;
    }
}
