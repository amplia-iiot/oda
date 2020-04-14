package es.amplia.oda.dispatcher.opengate.domain.update;

import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateOperation extends Request {
	private ParameterUpdateOperation parameters;
}
