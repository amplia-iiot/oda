package es.amplia.oda.comms.http;

import java.io.IOException;
import java.util.Map;

import es.amplia.oda.core.commons.http.HttpClientFactory;

public class HttpClientFactoryImpl implements HttpClientFactory {
    
    @Override
    public HttpClientImpl createHttpClient() {
        return createHttpClient(false);
    }

    @Override
    public HttpClientImpl createHttpClient(boolean insecure) {
        return new HttpClientImpl(insecure);
    }

    public HttpResponse post(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException {
        return post(url, payload, contentType, headers, false, false);
    }

    public HttpResponse post(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress, boolean insecure) throws IOException {
        HttpClientImpl client = new HttpClientImpl(insecure);
        return client.post(url, payload, contentType, headers, compress);
    }

    public HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException {
        return put(url, payload, contentType, headers, false, false);
    }

    public HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress, boolean insecure) throws IOException {
        HttpClientImpl client = new HttpClientImpl(insecure);
        return client.put(url, payload, contentType, headers, compress);
    }

    public HttpResponse get(String url, Map<String, String> headers) throws IOException {
        return get(url, headers, false);
    }

    public HttpResponse get(String url, Map<String, String> headers, boolean insecure) throws IOException {
        HttpClientImpl client = new HttpClientImpl(insecure);
        return client.get(url, headers);
    }

    public HttpResponse delete(String url, Map<String, String> headers) throws IOException {
        return delete(url, headers, false);
    }

    public HttpResponse delete(String url, Map<String, String> headers, boolean insecure) throws IOException {
        HttpClientImpl client = new HttpClientImpl(insecure);
        return client.delete(url, headers);
    }
}
