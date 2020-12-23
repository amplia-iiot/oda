package es.amplia.oda.dispatcher.opengate.domain.custom;

import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestCustomOperation extends Request {
	private Map<String, Object> parameters;
}
