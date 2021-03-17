package es.amplia.oda.dispatcher.opengate.domain.setclock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParameterSetClockOperation {
	private Datetime datetime;
}
