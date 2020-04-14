package es.amplia.oda.dispatcher.opengate.domain.get;

import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestGetOperation extends Request {
    private ParameterGetOperation parameters;

    public RequestGetOperation(String id, Long timestamp, String deviceId, String[] path, String name, ParameterGetOperation parameters) {
        super(id, timestamp, deviceId, path, name);
        this.parameters = parameters;
    }
}
