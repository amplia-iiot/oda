package es.amplia.oda.dispatcher.opengate.domain.setorconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputOperationSetOrConfigureOperation {
    private RequestSetOrConfigureOperation request;
}
