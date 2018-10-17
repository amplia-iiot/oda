package es.amplia.oda.dispatcher.opengate.domain;

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
    List<OutputVariable> response;
}