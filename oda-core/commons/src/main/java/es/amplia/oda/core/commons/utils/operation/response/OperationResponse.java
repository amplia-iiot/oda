package es.amplia.oda.core.commons.utils.operation.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OperationResponse {
    String version;
    Operation operation;
}