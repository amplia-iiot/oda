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

    //I'm not sure if a Visitor Pattern is the right thing to do, but that's my bet.
    //Why? Because this should have been a "domain" class, by no means having
    //logic implemented in it. So, this is a way to get the logic out of this class.
    //And because right now there is two virtual functions needed, and it seems
    //more plausible to need another one than to add another operation.
    //Time will tell... (If I had to trust experience, this is going to be a complete failure)
    public abstract void accept(RequestVisitor visitor);
}
