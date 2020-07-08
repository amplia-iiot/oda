package es.amplia.oda.dispatcher.opengate.domain.general;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterGeneralOperation {
	private List<Object> variableList;
	// Nothing is needed to do this operations.
}
