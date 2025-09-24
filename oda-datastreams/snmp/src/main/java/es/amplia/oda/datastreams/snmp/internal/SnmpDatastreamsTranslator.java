package es.amplia.oda.datastreams.snmp.internal;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SnmpDatastreamsTranslator implements SnmpTranslator {

    List<SnmpEntry> currentSnmpEntries;

    public SnmpDatastreamsTranslator(List<SnmpEntry> currentSnmpEntries) {
        this.currentSnmpEntries = currentSnmpEntries;
    }

    @Override
    public SnmpEntry translate(String OID, String deviceId) {

        List<SnmpEntry> entriesMatching = currentSnmpEntries.stream()
                .filter(entry -> entry.getOID().equalsIgnoreCase(OID) && entry.getDeviceId().equalsIgnoreCase(deviceId))
                .collect(Collectors.toList());

        if (entriesMatching.isEmpty()) {
            log.warn("There is no translation for device {} and OID {}", deviceId, OID);
            return null;
        }

        if (entriesMatching.size() > 1) {
            log.warn("There is more than one translation for device {} and OID {} : {}. Returning the first match.",
                    deviceId, OID, entriesMatching);
        }

        return entriesMatching.get(0);
    }
}
