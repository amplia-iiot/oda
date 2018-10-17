package es.amplia.oda.dispatcher.opengate.domain;

import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RequestGetDeviceParameters extends Request {

    private final List<Parameter> parameters;

    public RequestGetDeviceParameters(String id, String deviceId, Long timestamp, List<Parameter> parameters) {
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
        private ValueArray value;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueArray {
        List<VariableListElement> array;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VariableListElement {
        String variableName;
    }

}
