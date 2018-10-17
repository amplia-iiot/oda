package es.amplia.oda.dispatcher.opengate.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequestRefreshInfo extends Request {

    public RequestRefreshInfo(String id, String deviceId, Long timestamp) {
        super(id, deviceId, timestamp);
    }

    @Override
    public void accept(RequestVisitor visitor) {
        visitor.visit(this);
    }
}
