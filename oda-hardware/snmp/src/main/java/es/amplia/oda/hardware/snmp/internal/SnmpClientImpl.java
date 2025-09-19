package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;

@Slf4j
public class SnmpClientImpl implements SnmpClient {

    Snmp snmpClient;
    CommunityTarget communityTarget;
    UserTarget userTarget;

    String deviceId;

    public SnmpClientImpl(Snmp snmpClient, CommunityTarget target, String deviceId) {
        this.snmpClient = snmpClient;
        this.communityTarget = target;
        this.deviceId = deviceId;
    }

    public SnmpClientImpl(Snmp snmpClient, UserTarget userTarget, String deviceId) {
        this.snmpClient = snmpClient;
        this.userTarget = userTarget;
        this.deviceId = deviceId;
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }
}
