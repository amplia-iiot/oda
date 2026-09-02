package es.amplia.oda.operation.nashorn.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OperationEngineConfiguration {
	String path;
	String utilsPath;
}
