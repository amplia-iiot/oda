package es.amplia.oda.datastreams.snmp.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
class SnmpDatastreamsSetter implements DatastreamsSetter {

    private final String datastreamId;
    private final String deviceId;
    private final String OID;
    private final String dataType;
    SnmpClientsFinder clientsFinder;


    SnmpDatastreamsSetter(SnmpClientsFinder snmpClientsFinder, String OID, String dataType, String datastreamId,
                          String deviceId) {
        this.clientsFinder = snmpClientsFinder;
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
        this.OID = OID;
        this.dataType = dataType;
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public Type getDatastreamType() {
        return dataType.getClass();
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return Collections.singletonList(deviceId);
    }

    @Override
    public CompletableFuture<Void> set(String device, Object value) {
        log.info("Setting value {} to datastream {} of device {}", value, this.datastreamId, device);
        SnmpClient client = clientsFinder.getSnmpClient(device);
        client.setValue(this.OID, this.dataType, value.toString());
        return CompletableFuture.completedFuture(null);
    }
}
