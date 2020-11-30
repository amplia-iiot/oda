package es.amplia.oda.operation.synchronizeclock.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SynchronizeConfiguration {
	String clockDatastream;
}
