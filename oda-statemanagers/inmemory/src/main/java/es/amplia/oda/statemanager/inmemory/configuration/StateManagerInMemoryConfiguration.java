package es.amplia.oda.statemanager.inmemory.configuration;

import com.sun.istack.internal.NotNull;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StateManagerInMemoryConfiguration {
	@NotNull
	String databasePath;
}
