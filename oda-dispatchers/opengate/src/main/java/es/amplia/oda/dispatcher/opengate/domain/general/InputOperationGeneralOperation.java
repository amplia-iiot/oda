package es.amplia.oda.dispatcher.opengate.domain.general;

import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputOperationGeneralOperation {
    private RequestGeneralOperation request;
}
