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

    public Event(@NonNull String datastreamId, @NonNull String deviceId, String[] path, String feed, Long at, @NonNull Object value) {
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
        this.path = path;
        this.feed = feed;
        this.value = value;

        if (at == null) {
            this.at = System.currentTimeMillis();
        } else {
            this.at = at;
        }
    }
}
