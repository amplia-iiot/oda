package es.amplia.oda.dispatcher.opengate.domain;

import es.amplia.oda.operation.api.OperationUpdate;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequestUpdate extends Request {

    private final List<Parameter> parameters;

    public RequestUpdate(String id, String deviceId, Long timestamp, List<Parameter> parameters) {
        super(id, deviceId, timestamp);
        this.parameters = parameters;
    }

    @Override
    public void accept(RequestVisitor visitor) {
        visitor.visit(this);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Parameter {
        private String name;
        private ValueType value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueType {
        String string;
        List<VariableListElement> array;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariableListElement {
        String name;
        String version;
        OperationUpdate.DeploymentElementType type;
        String downloadUrl;
        String path;
        OperationUpdate.DeploymentElementOperationType operation;
        OperationUpdate.DeploymentElementOption option;
        Long order;
    }
}
