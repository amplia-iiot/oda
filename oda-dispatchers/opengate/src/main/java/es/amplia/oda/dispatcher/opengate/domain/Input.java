package es.amplia.oda.dispatcher.opengate.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Input {
    private InputOperation operation;
}
