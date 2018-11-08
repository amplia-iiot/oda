package es.amplia.oda.dispatcher.opengate.domain;

import lombok.Data;

@Data
public abstract class Request {
    private final String id;
    private final String deviceId;
    private final Long timestamp;

    protected Request(String id, String deviceId, Long timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public abstract void accept(RequestVisitor visitor);
}
