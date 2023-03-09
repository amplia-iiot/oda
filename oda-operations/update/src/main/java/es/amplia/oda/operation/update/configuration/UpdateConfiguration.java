package es.amplia.oda.operation.update.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateConfiguration {
	String rulesPath;
	String rulesUtilsPath;
	String backupPath;
	String deployPath;
	String downloadsPath;
	String configurationPath;
}
