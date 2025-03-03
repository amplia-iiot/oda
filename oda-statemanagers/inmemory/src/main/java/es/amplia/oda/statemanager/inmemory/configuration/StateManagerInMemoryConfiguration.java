package es.amplia.oda.statemanager.inmemory.configuration;

import lombok.Builder;
import lombok.Value;

import java.beans.ConstructorProperties;

@Value
@Builder
public class StateManagerInMemoryConfiguration {
	String databasePath;
	int maxData;
	long forgetTime;
	long forgetPeriod;
	@Builder.Default
	int numThreads = 10;
	@Builder.Default
	int taskQueueSize = 1000;
}
