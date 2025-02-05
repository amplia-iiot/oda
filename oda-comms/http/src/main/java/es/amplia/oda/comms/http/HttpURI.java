package es.amplia.oda.comms.http;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class HttpURI {

    URIBuilder uriBuilder;

    public HttpURI(String url) throws URISyntaxException {
        this.uriBuilder = new URIBuilder(url);
    }

    public void addQueryParameter(String parameterName, String parameterValue) {
        this.uriBuilder.addParameter(parameterName, parameterValue);
    }

    public String get() throws URISyntaxException {
        return this.uriBuilder.build().toString();
    }
}
