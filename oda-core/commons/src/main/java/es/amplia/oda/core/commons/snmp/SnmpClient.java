package es.amplia.oda.core.commons.snmp;

public interface SnmpClient {

    String getDeviceId();

    String getValue(String OID);

    void setValue(String OID, String dataType, String newValue);

    void disconnect();
}
