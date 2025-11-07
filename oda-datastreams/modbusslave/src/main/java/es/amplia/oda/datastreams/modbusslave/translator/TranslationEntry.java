package es.amplia.oda.datastreams.modbusslave.translator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranslationEntry {
    int modbusAddress;
    String deviceId;
    String datastreamId;
    String feed;
    String dataType;
}
