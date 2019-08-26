package es.amplia.oda.event.api;

import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class Event {
    @NonNull
    private String datastreamId;
    @NonNull
    private String deviceId;
    private String[] path;
    private Long at;
    @NonNull
    private Object value;
}
