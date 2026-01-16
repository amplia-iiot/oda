package es.amplia.oda.core.commons.http;

import java.io.IOException;
import java.util.Map;

public interface HttpClient {

    HttpResponse post(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException;
    HttpResponse post(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress) throws IOException;
    HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException;
    HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress) throws IOException;
    HttpResponse get(String url, Map<String, String> headers) throws IOException;
    HttpResponse delete(String url, Map<String, String> headers) throws IOException;
}
