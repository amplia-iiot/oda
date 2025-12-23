package es.amplia.oda.datastreams.modbusslave.translator;

import lombok.Data;

@Data
public class TranslationEntry {
    int modbusAddress;
    String deviceId;
    String datastreamId;
    String feed;
    String dataType;
    Integer numRegistersToGet;

    public TranslationEntry(int modbusAddress, String deviceId, String datastreamId, String feed, String dataType) {
        this.modbusAddress = modbusAddress;
        this.deviceId = deviceId;
        this.datastreamId = datastreamId;
        this.feed = feed;
        this.dataType = dataType;
    }

    public TranslationEntry(int modbusAddress, String deviceId, String datastreamId, String feed, String dataType,
                            Integer numRegistersToGet) {
        this.modbusAddress = modbusAddress;
        this.deviceId = deviceId;
        this.datastreamId = datastreamId;
        this.feed = feed;
        this.dataType = dataType;
        this.numRegistersToGet = numRegistersToGet;
    }
}
