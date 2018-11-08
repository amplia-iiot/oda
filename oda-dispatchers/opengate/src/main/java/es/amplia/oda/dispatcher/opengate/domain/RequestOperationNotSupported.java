package es.amplia.oda.dispatcher.opengate.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequestOperationNotSupported extends Request {

    private final String operationName;

    public RequestOperationNotSupported(String id, String deviceId, Long timestamp, String operationName) {
        super(id, deviceId, timestamp);
        this.operationName = operationName;
    }

    @Override
    public void accept(RequestVisitor visitor) {
        visitor.visit(this);
    }
}
