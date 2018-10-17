package es.amplia.oda.dispatcher.opengate.datastreamdomain;

import lombok.Value;

@Value
public class Datapoint {
    private Long at;
    private Object value;
}
