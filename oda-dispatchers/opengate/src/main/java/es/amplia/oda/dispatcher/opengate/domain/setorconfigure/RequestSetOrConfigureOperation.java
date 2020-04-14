package es.amplia.oda.dispatcher.opengate.domain.setorconfigure;

import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestSetOrConfigureOperation extends Request {
	private ParameterSetOrConfigureOperation parameters;
}
