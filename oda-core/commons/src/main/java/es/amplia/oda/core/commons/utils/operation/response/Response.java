package es.amplia.oda.core.commons.utils.operation.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Response {
    String id;
    String deviceId;
    String[] path;
    String name;
    OperationResultCode resultCode;
    String resultDescription;
    List<Step> steps;
}