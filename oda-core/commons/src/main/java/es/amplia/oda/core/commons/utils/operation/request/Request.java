package es.amplia.oda.core.commons.utils.operation.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request<T> {
    private String id;
    private Long timestamp;
    private String deviceId;
    private String[] path;
    private String name;
    private T parameters;
}
