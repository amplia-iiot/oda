package es.amplia.oda.hardware.atmanager.api;

import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Value
public class ATEvent {
    private final String name;
    private final List<String> parameters;

    private ATEvent(String name, List<String> params) {
        assert (name.length() >= 2 && name.charAt(0) == '+'); //Event MUST begin with plus
        assert (!params.isEmpty());
        this.name = name.toUpperCase();
        this.parameters = params;
    }

    public static ATEvent event(String name, String... params) {
        return new ATEvent(name, Arrays.asList(params));
    }

    public static ATEvent event(String name, List<String> params) {
        return new ATEvent(name, params);
    }

    public String asWireString() {
        String p = parameters.stream()
                .map(v -> {
                    if (v.isEmpty()) return "";
                    if (v.matches("\\d+")) return v;
                    return '"' + v + '"';
                })
                .collect(Collectors.joining(","));

        return name + ": " + p;
    }

}