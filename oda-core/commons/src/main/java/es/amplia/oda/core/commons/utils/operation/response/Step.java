package es.amplia.oda.core.commons.utils.operation.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Step {
    String name;
    StepResultCode result;
    String description;
    Long timestamp;
    List<Object> response;
}