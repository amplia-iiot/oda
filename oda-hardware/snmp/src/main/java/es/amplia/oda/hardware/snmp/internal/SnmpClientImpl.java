package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.snmp.SnmpException;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;


import java.io.IOException;
import java.util.*;

@Slf4j
public class SnmpClientImpl implements SnmpClient {

    Snmp snmpClient;
    int snmpVersion;
    CommunityTarget communityTarget;
    UserTarget userTarget;
    String contextName;

    String deviceId;

    public SnmpClientImpl(Snmp snmpClient, int version, CommunityTarget target, String deviceId) {
        this.snmpClient = snmpClient;
        this.communityTarget = target;
        this.deviceId = deviceId;
        this.snmpVersion = version;
    }

    public SnmpClientImpl(Snmp snmpClient, int version, UserTarget userTarget, String contextName, String deviceId) {
        this.snmpClient = snmpClient;
        this.userTarget = userTarget;
        this.deviceId = deviceId;
        this.snmpVersion = version;
        this.contextName = contextName;
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public Object getValue(String OID) {
        log.info("Getting value in OID {}", OID);

        ResponseEvent eventResponse;
        try {
            if (this.snmpVersion == 1 || this.snmpVersion == 2) {
                PDU pdu = new PDU();
                pdu.setType(PDU.GET);
                pdu.add(new VariableBinding(new OID(OID)));
                eventResponse = this.snmpClient.send(pdu, this.communityTarget);
            } else if (this.snmpVersion == 3) {
                ScopedPDU pdu = new ScopedPDU();
                pdu.setType(PDU.GET);
                pdu.setContextName(new OctetString(contextName));
                pdu.add(new VariableBinding(new OID(OID)));
                eventResponse = this.snmpClient.send(pdu, this.userTarget);
            } else {
                throw new SnmpException("Wrong snmp version : " + this.snmpVersion);
            }

            // parse response
            List<VariableBinding> responseValues = parseResponse(eventResponse);
            for (VariableBinding varBinding : responseValues) {
                return varBinding.getVariable().toString();
            }
        } catch (Exception e) {
            log.error("Error in get request : ", e);
        }

        return null;
    }

    @Override
    public void setValue(String OID, String dataType, String newValue){
        log.info("Setting value {} in OID {}", newValue, OID);
        ResponseEvent eventResponse;
        try {
            VariableBinding variableBind = new VariableBinding(new OID(OID), parseValueType(dataType, newValue));
            if (this.snmpVersion == 1 || this.snmpVersion == 2) {
                PDU pdu = new PDU();
                pdu.setType(PDU.SET);
                pdu.add(variableBind);
                eventResponse = this.snmpClient.send(pdu, this.communityTarget);
            } else if (this.snmpVersion == 3) {
                ScopedPDU pdu = new ScopedPDU();
                pdu.setType(PDU.SET);
                pdu.setContextName(new OctetString(contextName));
                pdu.add(variableBind);
                eventResponse = this.snmpClient.send(pdu, this.userTarget);
            } else {
                throw new SnmpException("Wrong snmp version : " + this.snmpVersion);
            }

            // check response
            checkResponse(eventResponse);
        } catch (Exception e) {
            log.error("Error in set request : ", e);
        }
    }

    @Override
    public void disconnect(){
        try {
            this.snmpClient.close();
        } catch (IOException e) {
            log.error("Error closing snmp client : ", e);
        }
    }

    private List<VariableBinding> parseResponse(ResponseEvent event) throws Exception {
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

    private PDU checkResponse(ResponseEvent event) throws Exception {
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

    private Variable parseValueType(String type, String value) throws IllegalArgumentException {
        switch (type.toUpperCase()) {
            case SnmpDataTypes.OBJECT_IDENTIFIER:
                return new OID(value);
            case SnmpDataTypes.INTEGER:
                return new Integer32(Integer.parseInt(value));
            case SnmpDataTypes.BIT_STRING:
            case SnmpDataTypes.OCTET_STRING:
                return new OctetString(value);
            case SnmpDataTypes.GAUGE32:
                return new Gauge32(Long.parseLong(value));
            case SnmpDataTypes.COUNTER32:
                return new Counter32(Long.parseLong(value));
            case SnmpDataTypes.COUNTER64:
                return new Counter64(Long.parseLong(value));
            case SnmpDataTypes.TIMETICKS:
                return new TimeTicks(Long.parseLong(value));
            case SnmpDataTypes.OPAQUE:
                return new Opaque(value.getBytes());
            case SnmpDataTypes.IPADDRESS:
                return new IpAddress(value);
            default:
                throw new IllegalArgumentException("Unsupported variable type " + type);
        }
    }

}
