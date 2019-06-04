package es.amplia.oda.dispatcher.opengate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private String id;
    private Long timestamp;
    private String deviceId;
    private String[] path;
    private String name;
    private List<Parameter> parameters;
}
