package es.amplia.oda.datastreams.modbusslave.translator;

import lombok.Data;

@Data
public class TranslationEntry {
    int startModbusAddress;
    int endModbusAddress;
    String deviceId;
    String datastreamId;
    String feed;
    String dataType;

    public TranslationEntry(int startModbusAddress, int endModbusAddress, String deviceId, String datastreamId,
                            String feed, String dataType) {
        this.startModbusAddress = startModbusAddress;
        this.endModbusAddress = endModbusAddress;
        this.deviceId = deviceId;
        this.datastreamId = datastreamId;
        this.feed = feed;
        this.dataType = dataType;
    }
}
