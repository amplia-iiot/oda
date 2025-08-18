package es.amplia.oda.datastreams.deviceinfo.datastreams;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClockInfo {
    String date;
    String time;
    String timezone;
    long dst;
}
