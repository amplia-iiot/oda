package es.amplia.oda.dispatcher.opengate.datastreamdomain;

import lombok.Value;

import java.util.List;

@Value
public class OutputDatastream {
    String version;
    String device;
    String[] path;
    List<Datastream> datastreams;
}
