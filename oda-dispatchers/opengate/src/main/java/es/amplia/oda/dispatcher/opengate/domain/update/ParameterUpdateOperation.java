package es.amplia.oda.dispatcher.opengate.domain.update;

import es.amplia.oda.operation.api.OperationUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterUpdateOperation {
	private String bundleName;
	private String bundleVersion;
	private List<OperationUpdate.DeploymentElement> deploymentElements;
}
