package es.amplia.oda.dispatcher.opengate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Parameter {
    private String name;
    private String type;
    private ValueObject value;
}
