package es.amplia.oda.datastreams.snmp.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SnmpDatastreamsGetter implements DatastreamsGetter {

    private final String datastreamId;
    private final String deviceId;
    private final String feed;
    private final String OID;
    private final Type dataType;
    SnmpClientsFinder clientsFinder;

    public SnmpDatastreamsGetter(SnmpClientsFinder snmpClientsFinder, String OID, Type dataType, String datastreamId,
                                 String deviceId, String feed) {
        this.clientsFinder = snmpClientsFinder;
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
        this.feed = feed;
        this.OID = OID;
        this.dataType = dataType;
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return Collections.singletonList(deviceId);
    }

    @Override
    public CompletableFuture<CollectedValue> get(String device) {
        SnmpClient client = clientsFinder.getSnmpClient(device);
        Object valueRetrieved = client.getValue(this.OID);
        return CompletableFuture.completedFuture(new CollectedValue(System.currentTimeMillis(), valueRetrieved,
                null, this.feed));
    }

}
