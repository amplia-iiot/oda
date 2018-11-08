package es.amplia.oda.dispatcher.opengate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutputVariable {
    String variableName;
    Object variableValue;
    String resultCode;
    String resultDescription;
}