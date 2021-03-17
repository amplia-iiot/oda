package es.amplia.oda.dispatcher.opengate.domain.setclock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputOperationSetClockOperation {
    private RequestSetClockOperation request;
}
