package es.amplia.oda.dispatcher.opengate.domain.general;

import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.InputOperationSetOrConfigureOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputGeneralOperation {
    private InputOperationGeneralOperation operation;
}
