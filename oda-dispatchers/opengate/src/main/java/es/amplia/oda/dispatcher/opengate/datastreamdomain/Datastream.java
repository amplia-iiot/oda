package es.amplia.oda.dispatcher.opengate.datastreamdomain;

import lombok.Value;

import java.util.Set;

@Value
public class Datastream {
    private String id;
    private Set<Datapoint> datapoints;
}
