package es.amplia.oda.comms.iec104;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Iec104CacheValue {

    // value of ASDU received
    private Object value;
    // time when ASDU has been received
    private long valueTime;
    // indicates if value has already been processed or is it new
    private boolean processed;
}
