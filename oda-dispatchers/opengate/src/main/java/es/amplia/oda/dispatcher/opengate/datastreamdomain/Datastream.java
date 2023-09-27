package es.amplia.oda.dispatcher.opengate.datastreamdomain;


import lombok.Value;

import java.util.Set;

@Value
public class Datastream {
    String id;
    String feed;
    Set<Datapoint> datapoints;
}
