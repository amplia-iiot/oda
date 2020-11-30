package es.amplia.oda.operation.setclock.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetClockConfiguration {
	String clockDatastream;
}
