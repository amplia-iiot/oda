package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.snmp.SnmpEntry;

public interface SnmpTranslator {

    /**
     * Get the datastreamId and other data associated to the OID and device
     */
    SnmpEntry translate(String OID, String deviceId);
}
