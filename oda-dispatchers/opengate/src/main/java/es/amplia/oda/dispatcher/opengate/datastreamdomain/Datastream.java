package es.amplia.oda.dispatcher.opengate.datastreamdomain;


import lombok.Value;

import java.util.List;

@Value
public class Datastream {
    String id;
    String feed;
    List<Datapoint> datapoints;
}
