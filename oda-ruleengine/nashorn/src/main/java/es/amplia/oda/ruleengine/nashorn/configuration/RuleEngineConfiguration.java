package es.amplia.oda.ruleengine.nashorn.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RuleEngineConfiguration {
	String path;
	String utilsPath;
}
