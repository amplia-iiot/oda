package es.amplia.oda.dispatcher.opengate.domain.setorconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterSetOrConfigureOperation {
	private List<ValueSetting> variableList;
}
