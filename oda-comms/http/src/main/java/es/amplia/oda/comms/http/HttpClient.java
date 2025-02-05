package es.amplia.oda.comms.http;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    private static final String GZIP_ENCODING = "gzip";
    private static final int OK_HTTP_CODE = 200;
    private static final int CREATED_HTTP_CODE = 201;
    private static final int FOUND_HTTP_CODE = 302;

    private CloseableHttpClient httpClient;
    private RequestConfig requestTimeoutConfig;

    public HttpClient() {
        this(false);
    }

    public HttpClient(boolean insecure) {
        if (insecure) {
            this.httpClient = httpClientSSLNoVerify();
        } else {
            this.httpClient = HttpClients.createDefault();
        }
        setTimeout(10000);
    }

    private CloseableHttpClient httpClientSSLNoVerify() {

        TrustManager trm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {

            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        };

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trm }, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        return HttpClients.custom().setSSLContext(sslContext).build();
    }

    public void setTimeout(int timeout) {
        this.requestTimeoutConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }

    private boolean isSuccessCode(int statusCode) {
        return statusCode == OK_HTTP_CODE || statusCode == CREATED_HTTP_CODE || statusCode == FOUND_HTTP_CODE;
    }

    public HttpResponse post(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException {
        return post(url, payload, contentType, headers, false);
    }

    public HttpResponse post(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress) throws IOException {
        EntityBuilder entityBuilder =
                    EntityBuilder.create().setBinary(payload).setContentType(ContentType.create(contentType));

        if (compress) {
            LOGGER.debug("Compressing data");
            entityBuilder.setContentEncoding(GZIP_ENCODING).gzipCompress();
        }

        HttpEntity entity = entityBuilder.build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        httpPost.setConfig(requestTimeoutConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpPost::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        HashMap<String, String> rHeaders = new HashMap<>();
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP POST message sent");
            entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
            Header[] headersResp = httpResponse.getAllHeaders();
			for (int i = 0; i < headersResp.length; i++) {
				Header h = headersResp[i];
				rHeaders.put(h.getName(), h.getValue());
			}
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP POST message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).headers(rHeaders).build();
    }

    public HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException {
        return put(url, payload, contentType, headers, false);
    }

    public HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress) throws IOException {
        EntityBuilder entityBuilder =
                    EntityBuilder.create().setBinary(payload).setContentType(ContentType.create(contentType));

        if (compress) {
            LOGGER.debug("Compressing data");
            entityBuilder.setContentEncoding(GZIP_ENCODING).gzipCompress();
        }

        HttpEntity entity = entityBuilder.build();

        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(entity);
        httpPut.setConfig(requestTimeoutConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpPut::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpPut);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        HashMap<String, String> rHeaders = new HashMap<>();
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP PUT message sent");
            entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
            Header[] headersResp = httpResponse.getAllHeaders();
			for (int i = 0; i < headersResp.length; i++) {
				Header h = headersResp[i];
				rHeaders.put(h.getName(), h.getValue());
			}
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP PUT message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).headers(rHeaders).build();
    }

    public HttpResponse get(String url, Map<String, String> headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestTimeoutConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpGet::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP GET message sent");
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP GET message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).build();
    }

    public HttpResponse delete(String url, Map<String, String> headers) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(requestTimeoutConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpDelete::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP DELETE message sent");
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP DELTE message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).build();
    }

}
