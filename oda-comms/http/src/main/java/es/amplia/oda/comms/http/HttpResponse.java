package es.amplia.oda.comms.http;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HttpResponse {
    private String response;
    private int statusCode;
    private Map<String, String> headers;
}
