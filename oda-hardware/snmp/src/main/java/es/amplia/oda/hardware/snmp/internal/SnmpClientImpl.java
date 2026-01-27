package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.snmp.SnmpException;
import es.amplia.oda.hardware.snmp.SnmpCounters;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.*;


import java.io.IOException;
import java.util.*;

@Slf4j
public class SnmpClientImpl implements SnmpClient {

    private final Snmp snmpClient;
    private final int snmpVersion;
    private CommunityTarget communityTarget;
    private UserTarget userTarget;
    private String contextName;
    private final String deviceId;

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

            // incr counter
            SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_GET_OK, this.deviceId, 1);

            // get values in response
            for (VariableBinding varBinding : responseValues) {
                return SnmpDataTypes.parseVariable(varBinding.getVariable());
            }

        } catch (Exception e) {
            log.error("Error in get request : ", e);
            // incr counter
            SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_GET_ERROR, this.deviceId, 1);
        }

        return null;
    }

    @Override
    public void setValue(String OID, String dataType, String newValue){
        log.info("Setting value {} in OID {}", newValue, OID);
        ResponseEvent eventResponse;
        try {
            VariableBinding variableBind = new VariableBinding(new OID(OID), SnmpDataTypes.formatValue(dataType, newValue));
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
            getResponse(eventResponse);

            // incr counter
            SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_SET_OK, this.deviceId, 1);

        } catch (Exception e) {
            log.error("Error in set request : ", e);

            // incr counter
            SnmpCounters.incrCounter(SnmpCounters.SnmpCounterType.SNMP_SET_ERROR, this.deviceId, 1);
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
        // get response
        PDU response = getResponse(event);

        // retrieve values in response
        Vector<? extends VariableBinding> varBindings = response.getVariableBindings();
        return new ArrayList<>(varBindings);
    }

    private PDU getResponse(ResponseEvent event) throws Exception {
        // check error
        Exception error = event.getError();
        if (error != null) {
            log.error("Error in response event : ", error);
            throw error;
        }

        // get response
        PDU response = event.getResponse();
        if (response == null) {
            throw new SnmpException("Response PDU is null");
        }

        //log.info("Response = {}", response);

        if (response.getErrorStatus() != 0) {
            throw new SnmpException("Error Status = " + response.getErrorStatus());
        }
        return response;
    }

}
