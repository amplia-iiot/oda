package es.amplia.oda.dispatcher.opengate.domain.setclock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Datetime {
	private String date;
	private String time;
	private int timezone;
	private int dst;
}
