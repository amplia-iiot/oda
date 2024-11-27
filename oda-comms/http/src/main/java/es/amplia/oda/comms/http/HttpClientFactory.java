package es.amplia.oda.comms.http;

public interface HttpClientFactory {

    public HttpClient createClient();

    public HttpClient createClient(boolean insecure);

}
