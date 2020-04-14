package es.amplia.oda.dispatcher.opengate.domain.general;

import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestGeneralOperation extends Request {
    private ParameterGeneralOperation parameters;
}
