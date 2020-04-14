package es.amplia.oda.dispatcher.opengate.domain.interfaces;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Request {
    private String id;
    private Long timestamp;
    private String deviceId;
    private String[] path;
    private String name;
}
