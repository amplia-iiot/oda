package es.amplia.oda.dispatcher.opengate.domain.get;

import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputOperationGetOperation {
    private RequestGetOperation request;
}
