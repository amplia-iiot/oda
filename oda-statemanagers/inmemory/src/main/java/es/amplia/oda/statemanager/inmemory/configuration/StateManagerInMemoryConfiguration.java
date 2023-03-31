package es.amplia.oda.statemanager.inmemory.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StateManagerInMemoryConfiguration {
	String databasePath;
	int maxData;
	long forgetTime;
	long forgetPeriod;
}
