package es.amplia.oda.dispatcher.opengate.datastreamdomain;

import lombok.Value;

import java.util.Set;

@Value
public class OutputDatastream {
    private String version;
    private String device;
    private String[] path;
    private Set<Datastream> datastreams;
}
