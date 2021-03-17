package es.amplia.oda.dispatcher.opengate.domain.setclock;

import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestSetClockOperation extends Request {
	private ParameterSetClockOperation parameters;
}
