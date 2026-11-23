package es.amplia.oda.comms.http;

import es.amplia.oda.comms.http.configuration.HttpClientConfiguration;
import es.amplia.oda.core.commons.http.HttpResponse;
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

import javax.net.ssl.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpClientImpl implements HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientImpl.class);

    private static final String GZIP_ENCODING = "gzip";
    private static final int OK_HTTP_CODE = 200;
    private static final int CREATED_HTTP_CODE = 201;
    private static final int ACCEPTED_HTTP_CODE = 202;
    private static final int FOUND_HTTP_CODE = 302;
    static final int DEFAULT_TIMEOUT_MS = 10000;

    private final CloseableHttpClient httpClient;
    RequestConfig customConfig;

    public HttpClientImpl(HttpClientConfiguration httpClientConfig, boolean insecure) {
        CloseableHttpClient client = insecure ? httpClientSSLNoVerify() : httpClientSSL(httpClientConfig);
        if (client == null) {
            LOGGER.error("Falling back to default http client: JVM trusted CAs and no client certificate");
            client = HttpClients.createDefault();
        }
        this.httpClient = client;
        setTimeout(DEFAULT_TIMEOUT_MS);
    }

    // Returns null when the configured SSL material cannot be loaded.
    CloseableHttpClient httpClientSSL(HttpClientConfiguration config) {
        boolean hasKeyStore = config != null && config.getKeyStorePath() != null;
        boolean hasTrustStore = config != null && config.getTrustStorePath() != null;
        if (!hasKeyStore && !hasTrustStore) {
            return HttpClients.createDefault();
        }

        try {
            KeyManager[] keyManagers = null;
            if (hasKeyStore) {
                KeyStore keyStore = loadStore(config.getKeyStorePath(), config.getKeyStoreType(),
                        config.getKeyStorePassword());
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keyStore, toChars(config.getKeyStorePassword()));
                keyManagers = kmf.getKeyManagers();
            }

            // null keeps the JVM default CAs; a configured truststore is added to them, not a replacement
            TrustManager[] trustManagers = null;
            if (hasTrustStore) {
                KeyStore trustStore = loadStore(config.getTrustStorePath(), config.getTrustStoreType(),
                        config.getTrustStorePassword());
                trustManagers = new TrustManager[] {
                        new CompositeTrustManager(x509TrustManager(trustStore), x509TrustManager(null))
                };
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            return HttpClients.custom().setSSLContext(sslContext).build();
        } catch (Exception e) {
            LOGGER.error("Error loading SSL configuration (keyStore = {}, trustStore = {})",
                    config.getKeyStorePath(), config.getTrustStorePath(), e);
            return null;
        }
    }

    private static KeyStore loadStore(String path, String type, String password)
            throws IOException, GeneralSecurityException {
        KeyStore store = KeyStore.getInstance(type);
        try (InputStream in = new FileInputStream(path)) {
            store.load(in, toChars(password));
        }
        return store;
    }

    private static char[] toChars(String password) {
        return password == null ? null : password.toCharArray();
    }

    private static X509TrustManager x509TrustManager(KeyStore store) throws GeneralSecurityException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(store);
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        throw new KeyStoreException("No X509TrustManager available");
    }

    // Trusts a certificate if either the configured truststore or the JVM default CAs accept it.
    static final class CompositeTrustManager implements X509TrustManager {
        private final X509TrustManager configured;
        private final X509TrustManager jvmDefault;

        CompositeTrustManager(X509TrustManager configured, X509TrustManager jvmDefault) {
            this.configured = configured;
            this.jvmDefault = jvmDefault;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                configured.checkClientTrusted(chain, authType);
            } catch (CertificateException e) {
                jvmDefault.checkClientTrusted(chain, authType);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                configured.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                jvmDefault.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] a = configured.getAcceptedIssuers();
            X509Certificate[] b = jvmDefault.getAcceptedIssuers();
            X509Certificate[] all = Arrays.copyOf(a, a.length + b.length);
            System.arraycopy(b, 0, all, a.length, b.length);
            return all;
        }
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
            LOGGER.error("Error creating http client: ", e);
            return null;
        }

        // specify host verifier (NoopHostnameVerifier) to allow accepting certificates from different hosts
        SSLConnectionSocketFactory scsf;
        try {
            scsf = new SSLConnectionSocketFactory(
                    SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            LOGGER.error("Error creating http client: ", e);
            return null;
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
        if (checkHttpClientNotInit()) {
            return null;
        }

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
            HttpEntity rEntity = httpResponse.getEntity();
            if (rEntity != null) {
                response = EntityUtils.toString(rEntity);
            }
            Header[] headersResp = httpResponse.getAllHeaders();
            for (Header h : headersResp) {
                rHeaders.put(h.getName(), h.getValue());
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP POST message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder()
                .statusCode(statusCode)
                .response(response)
                .headers(rHeaders)
                .build();
    }

    public HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers) throws IOException {
        return put(url, payload, contentType, headers, false);
    }

    public HttpResponse put(String url, byte[] payload, String contentType, Map<String, String> headers, boolean compress) throws IOException {
        if (checkHttpClientNotInit()) {
            return null;
        }

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
            HttpEntity rEntity = httpResponse.getEntity();
            if (rEntity != null) {
                response = EntityUtils.toString(rEntity);
            }
            Header[] headersResp = httpResponse.getAllHeaders();
            for (Header h : headersResp) {
                rHeaders.put(h.getName(), h.getValue());
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP PUT message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder()
                .statusCode(statusCode)
                .response(response)
                .headers(rHeaders)
                .build();
    }

    public HttpResponse get(String url, Map<String, String> headers) throws IOException {
        if (checkHttpClientNotInit()) {
            return null;
        }

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
            HttpEntity rEntity = httpResponse.getEntity();
            if (rEntity != null) {
                response = EntityUtils.toString(rEntity);
            }
            Header[] headersResp = httpResponse.getAllHeaders();
            for (Header h : headersResp) {
                rHeaders.put(h.getName(), h.getValue());
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP GET message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder()
                .statusCode(statusCode)
                .response(response)
                .headers(rHeaders)
                .build();
    }

    public HttpResponse delete(String url, Map<String, String> headers) throws IOException {
        if (checkHttpClientNotInit()) {
            return null;
        }

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
            HttpEntity rEntity = httpResponse.getEntity();
            if (rEntity != null) {
                response = EntityUtils.toString(rEntity);
            }
            Header[] headersResp = httpResponse.getAllHeaders();
            for (Header h : headersResp) {
                rHeaders.put(h.getName(), h.getValue());
            }
        } else {
            response = httpResponse.getStatusLine().getReasonPhrase();
            LOGGER.error("Error sending HTTP DELTE message: {}, {}", statusCode, response);
        }

        httpResponse.close();

        return HttpResponse.builder()
                .statusCode(statusCode)
                .response(response)
                .headers(rHeaders)
                .build();
    }

    public byte[] getBinaryFile(String url, Map<String, String> headers) throws IOException {
        byte[] bytesResponse = new byte[0];

        if (checkHttpClientNotInit()) {
            return bytesResponse;
        }

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(customConfig);
        if(headers != null && !headers.isEmpty()) {
            headers.forEach(httpGet::setHeader);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        // check response
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (isSuccessCode(statusCode)) {
            LOGGER.debug("HTTP GET message sent");
            // get headers
            Header[] headersResp = httpResponse.getAllHeaders();

            // get body
            HttpEntity rEntity = httpResponse.getEntity();
            if (rEntity != null) {
                InputStream in = rEntity.getContent();

                // read input stream to byte array
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] temp = new byte[1024];
                int bytesLeidos;
                while ((bytesLeidos = in.read(temp)) != -1) {
                    buffer.write(temp, 0, bytesLeidos);
                }
                bytesResponse = buffer.toByteArray();
            }
        } else {
            LOGGER.error("Error sending HTTP GET message: {}", statusCode);
        }

        httpResponse.close();

        return bytesResponse;
    }

    private boolean checkHttpClientNotInit() {
        if (httpClient == null) {
            LOGGER.error("Http client not initialized. Can´t send request");
            return true;
        }
        return false;
    }

}
