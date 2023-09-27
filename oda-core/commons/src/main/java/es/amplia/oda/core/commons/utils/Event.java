package es.amplia.oda.core.commons.utils;

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
    private String feed;
    private Long at;
    @NonNull
    private Object value;
}
