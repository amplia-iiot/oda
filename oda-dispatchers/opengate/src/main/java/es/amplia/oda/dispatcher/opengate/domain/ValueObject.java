package es.amplia.oda.dispatcher.opengate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueObject {
    String string;
    Double number;
    Object object;
    List<Object> array;
}
