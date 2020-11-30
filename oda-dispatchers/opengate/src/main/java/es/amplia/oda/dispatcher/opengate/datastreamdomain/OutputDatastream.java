package es.amplia.oda.dispatcher.opengate.datastreamdomain;

import lombok.Value;

import java.util.Set;

@Value
public class OutputDatastream {
    String version;
    String device;
    String[] path;
    Set<Datastream> datastreams;
}
