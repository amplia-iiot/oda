package es.amplia.oda.core.commons.http;

public interface HttpClientFactory {

    public HttpClient createHttpClient();
    public HttpClient createHttpClient(boolean insecure);
}
