package es.amplia.oda.dispatcher.opengate.domain.setorconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueSetting {
	private String name;
	private String value;
}
