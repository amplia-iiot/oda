package es.amplia.oda.comms.http;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.core.commons.entities.ContentType;

public class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    private static final String UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE = "application/x-msgpack";
    private static final String CBOR_MEDIA_TYPE = "application/cbor";
    private static final Map<ContentType, org.apache.http.entity.ContentType> CONTENT_TYPE_MAPPER =
            new EnumMap<>(ContentType.class);
    static {
        CONTENT_TYPE_MAPPER.put(ContentType.CBOR, org.apache.http.entity.ContentType.create(CBOR_MEDIA_TYPE));
        CONTENT_TYPE_MAPPER.put(ContentType.JSON, org.apache.http.entity.ContentType.APPLICATION_JSON);
        CONTENT_TYPE_MAPPER.put(ContentType.MESSAGE_PACK,
                org.apache.http.entity.ContentType.create(UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE));
    }

    private static final String GZIP_ENCODING = "gzip";
    private static final int OK_HTTP_CODE = 200;
    private static final int CREATED_HTTP_CODE = 201;

    private CloseableHttpClient httpClient;
    private RequestConfig requestTimeoutConfig;

    public HttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        setTimeout(10000);
    }

    public void setTimeout(int timeout) {
        this.requestTimeoutConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();
    }

    private org.apache.http.entity.ContentType getContentType(ContentType contentType) {
        return CONTENT_TYPE_MAPPER.get(contentType);
    }

    private boolean isSuccessCode(int statusCode) {
        return statusCode == OK_HTTP_CODE || statusCode == CREATED_HTTP_CODE;
    }

    public HttpResponse post(String url, byte[] payload, ContentType contentType, Map<String, String> headers) throws IOException {
        return post(url, payload, contentType, headers, false);
    }

    public HttpResponse post(String url, byte[] payload, ContentType contentType, Map<String, String> headers, boolean compress) throws IOException {
        EntityBuilder entityBuilder =
                    EntityBuilder.create().setBinary(payload).setContentType(getContentType(contentType));

        if (compress) {
            LOGGER.debug("Compressing data");
            entityBuilder.setContentEncoding(GZIP_ENCODING).gzipCompress();
        }

        HttpEntity entity = entityBuilder.build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);
        httpPost.setConfig(requestTimeoutConfig);
        headers.forEach( (h,v) -> httpPost.setHeader(h, v));

        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP POST message sent");
            entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP POST message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).build();
    }

    public HttpResponse put(String url, byte[] payload, ContentType contentType, Map<String, String> headers) throws IOException {
        return put(url, payload, contentType, headers, false);
    }

    public HttpResponse put(String url, byte[] payload, ContentType contentType, Map<String, String> headers, boolean compress) throws IOException {
        EntityBuilder entityBuilder =
                    EntityBuilder.create().setBinary(payload).setContentType(getContentType(contentType));

        if (compress) {
            LOGGER.debug("Compressing data");
            entityBuilder.setContentEncoding(GZIP_ENCODING).gzipCompress();
        }

        HttpEntity entity = entityBuilder.build();

        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(entity);
        httpPut.setConfig(requestTimeoutConfig);
        headers.forEach( (h,v) -> httpPut.setHeader(h, v));

        CloseableHttpResponse httpResponse = httpClient.execute(httpPut);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String response = null;
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP PUT message sent");
            entity = httpResponse.getEntity();
            if (entity != null) {
                response = EntityUtils.toString(entity);
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP PUT message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder().statusCode(statusCode).response(response).build();
    }

    public HttpResponse get(String url, Map<String, String> headers) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestTimeoutConfig);
        headers.forEach( (h,v) -> httpGet.setHeader(h, v));

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
        headers.forEach( (h,v) -> httpDelete.setHeader(h, v));

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
