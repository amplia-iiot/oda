package es.amplia.oda.event.api;

import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class Event {
    private String datastreamId;
    private String deviceId;
    private String[] path;
    private Long at;
    private Object value;
}
