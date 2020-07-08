package es.amplia.oda.dispatcher.opengate.domain.get;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterGetOperation {
    private List<String> variableList;
}
