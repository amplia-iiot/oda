package es.amplia.oda.comms.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.core.commons.http.HttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class HttpClientImpl implements HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientImpl.class);

    private static final String GZIP_ENCODING = "gzip";
    private static final int OK_HTTP_CODE = 200;
    private static final int CREATED_HTTP_CODE = 201;
    private static final int ACCEPTED_HTTP_CODE = 202;
    private static final int FOUND_HTTP_CODE = 302;

    private final CloseableHttpClient httpClient;
    private RequestConfig customConfig;

    public HttpClientImpl() {
        this(false);
    }

    public HttpClientImpl(boolean insecure) {
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

        // specify host verifier (NoopHostnameVerifier) to allow accepting certificates from different hosts
        SSLConnectionSocketFactory scsf;
        try {
            scsf = new SSLConnectionSocketFactory(
                    SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return HttpClients.custom().setSSLContext(sslContext).setSSLSocketFactory(scsf).build();
    }

    public void setTimeout(int timeout) {
        RequestConfig.Builder customConfigBuilder;
        if (this.customConfig != null) {
            customConfigBuilder = RequestConfig.copy(this.customConfig);
        } else {
            customConfigBuilder = RequestConfig.custom();
        }

        this.customConfig = customConfigBuilder
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }

    public void setRequestCustomConfig(HttpConfig customConfig) {
        RequestConfig.Builder customConfigBuilder;
        if (this.customConfig != null) {
            customConfigBuilder = RequestConfig.copy(this.customConfig);
        } else {
            customConfigBuilder = RequestConfig.custom();
        }

        this.customConfig = customConfigBuilder
                .setConnectTimeout(customConfig.getTimeout())
                .setConnectionRequestTimeout(customConfig.getTimeout())
                .setSocketTimeout(customConfig.getTimeout())
                .setCookieSpec(HttpConfig.getCookiesSpec(customConfig.getCookiesPolicy()))
                .build();
    }

    public void setCookies(String cookiesPolicy) {
        RequestConfig.Builder customConfigBuilder;
        if (this.customConfig != null) {
            customConfigBuilder = RequestConfig.copy(this.customConfig);
        } else {
            customConfigBuilder = RequestConfig.custom();
        }
        this.customConfig = customConfigBuilder
                .setCookieSpec(HttpConfig.getCookiesSpec(cookiesPolicy))
                .build();
    }

    private boolean isSuccessCode(int statusCode) {
        // if it is a 2XX code, it is OK
        if (statusCode / 100 == 2) {
            return true;
        } else if (statusCode == FOUND_HTTP_CODE) {
            return true;
        } else {
            return false;
        }
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
        httpPost.setConfig(customConfig);
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
        httpPut.setConfig(customConfig);
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
        httpGet.setConfig(customConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpGet::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        HashMap<String, String> rHeaders = new HashMap<>();
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP GET message sent");
            HttpEntity entity = httpResponse.getEntity();
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
            LOGGER.error("Error sending HTTP GET message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).headers(rHeaders).build();
    }

    public HttpResponse delete(String url, Map<String, String> headers) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setConfig(customConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpDelete::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        HashMap<String, String> rHeaders = new HashMap<>();
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP DELETE message sent");
            HttpEntity entity = httpResponse.getEntity();
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
            LOGGER.error("Error sending HTTP DELTE message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).headers(rHeaders).build();
    }

}
