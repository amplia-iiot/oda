package es.amplia.oda.datastreams.modbusslave.translator;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ModbusEventTranslator {

    private static List<TranslationEntry> entries = new ArrayList<>();

    public static void addEntry(TranslationEntry newEntry) {
        if (newEntry == null) {
            return;
        }

        if (entries == null) {
            entries = new ArrayList<>();
        }

        // check if it exists a block entry with the same address
        List<TranslationEntry> entriesMatching;
        if (newEntry.dataType.equalsIgnoreCase(ModbusToJavaTypeConverter.DATA_TYPE_LIST)) {
            entriesMatching = getExistingBlockTranslations(newEntry.startModbusAddress, newEntry.endModbusAddress, newEntry.deviceId);
            if (!entriesMatching.isEmpty()) {
                log.error("Entry for address range (start = {}, end = {}) for device {} already exists",
                        newEntry.getStartModbusAddress(), newEntry.getEndModbusAddress(), newEntry.getDeviceId());
                return;
            }

            // entry not exist, adding new entry
            log.info("Adding new entry for address range (start = {}, end = {}) of device {} : {}", newEntry.getStartModbusAddress(),
                    newEntry.getEndModbusAddress(), newEntry.getDeviceId(), newEntry);
        }
        // check if single entry already exists
        else {
            entriesMatching = getExistingNonBlockTranslations(newEntry.startModbusAddress, newEntry.deviceId);
            if (!entriesMatching.isEmpty()) {
                log.error("Entry for address {} for device {} already exists", newEntry.getStartModbusAddress(), newEntry.getDeviceId());
                return;
            }

            // entry not exist, adding new entry
            log.info("Adding new entry for address {} of device {} : {}", newEntry.getStartModbusAddress(), newEntry.getDeviceId(), newEntry);
        }

        // add entry
        entries.add(newEntry);
    }

    public static void clearAllEntries(){
        if (entries != null) {
            entries.clear();
        }
    }

    public static List<TranslationEntry> getExistingNonBlockTranslations(int modbusAddress, String deviceId) {
        if (entries == null) {
            return Collections.emptyList();
        }

        return entries.stream().filter(value -> value.getDeviceId().equalsIgnoreCase(deviceId)
                        && !value.getDataType().equalsIgnoreCase(ModbusToJavaTypeConverter.DATA_TYPE_LIST)
                        && value.getStartModbusAddress() == modbusAddress)
                .collect(Collectors.toList());
    }

    public static List<TranslationEntry> getExistingBlockTranslations(int startModbusAddress, int endModbusAddress, String deviceId) {
        if (entries == null) {
            return Collections.emptyList();
        }

        return entries.stream().filter(value -> value.getDeviceId().equalsIgnoreCase(deviceId)
                        && value.getDataType().equalsIgnoreCase(ModbusToJavaTypeConverter.DATA_TYPE_LIST)
                        && value.getStartModbusAddress() <= startModbusAddress && value.getEndModbusAddress() >= endModbusAddress)
                .collect(Collectors.toList());
    }
}
