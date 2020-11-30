package es.amplia.oda.dispatcher.opengate.datastreamdomain;

import lombok.Value;

@Value
public class Datapoint {
    Long at;
    Object value;
}
