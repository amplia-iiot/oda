package es.amplia.oda.datastreams.modbusslave.translator;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ModbusEventTranslator {

    private static List<TranslationEntry> entries = new ArrayList<>();

    public static TranslationEntry translate(int modbusAddress, String deviceId) {
        List<TranslationEntry> entriesMatching = getExistingEntries(modbusAddress, deviceId);
        if (entriesMatching.isEmpty()) {
            return null;
        }

        if (entriesMatching.size() > 1) {
            log.warn("There is more than one translation for address {} and device {}", modbusAddress, deviceId);
        }

        return entriesMatching.get(0);
    }

    public static void addEntry(TranslationEntry newEntry) {
        if(newEntry == null){
            return;
        }

        if (entries == null) {
            entries = new ArrayList<>();
        } else {
            // check if entry already exists
            List<TranslationEntry> entriesMatching = getExistingEntries(newEntry.modbusAddress, newEntry.deviceId);
            if (!entriesMatching.isEmpty()) {
                log.error("Entry with address {} for device {} already exists", newEntry.getModbusAddress(), newEntry.getDeviceId());
                return;
            }
        }

        // entry not exist, adding new entry
        log.info("Adding new entry for address {} of device {} : {}", newEntry.getModbusAddress(), newEntry.getDeviceId(), newEntry);
        entries.add(newEntry);
    }

    public static void clearAllEntries(){
        if (entries != null) {
            entries.clear();
        }
    }

    private static List<TranslationEntry> getExistingEntries(int modbusAddress, String deviceId) {
        if (entries == null) {
            return Collections.emptyList();
        }

        return entries.stream().filter(value -> value.getDeviceId().equalsIgnoreCase(deviceId)
                && value.getModbusAddress() == modbusAddress).collect(Collectors.toList());
    }
}
