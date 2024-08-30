package es.amplia.oda.connector.http;

import es.amplia.oda.connector.http.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public class HttpConnector implements OpenGateConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnector.class);

    static final String UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE = "application/x-msgpack";
    static final String CBOR_MEDIA_TYPE = "application/cbor";
    private static final Map<ContentType, org.apache.http.entity.ContentType> CONTENT_TYPE_MAPPER =
            new EnumMap<>(ContentType.class);
    static {
        CONTENT_TYPE_MAPPER.put(ContentType.CBOR, org.apache.http.entity.ContentType.create(CBOR_MEDIA_TYPE));
        CONTENT_TYPE_MAPPER.put(ContentType.JSON, org.apache.http.entity.ContentType.APPLICATION_JSON);
        CONTENT_TYPE_MAPPER.put(ContentType.MESSAGE_PACK,
                org.apache.http.entity.ContentType.create(UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE));
    }
    static final String HTTP_PROTOCOL = "http";
    static final String API_KEY_HEADER_NAME = "X-ApiKey";
    static final String GZIP_ENCODING = "gzip";

    static final int MILLISECONDS_PER_SECOND = 1000;
    static final int CONNECTION_TIMEOUT = 5;
    static final int OK_HTTP_CODE = 200;
    static final int CREATED_HTTP_CODE = 201;


    private final DeviceInfoProvider deviceInfoProvider;
    private URL hostUrl;
    private String generalPath;
    private String collectionPath;
    private boolean compressionEnabled;
    private int compressionThreshold;


    HttpConnector(DeviceInfoProvider deviceInfoProvider) {
        this.deviceInfoProvider = deviceInfoProvider;
    }

    public void loadConfiguration(ConnectorConfiguration configuration) {
        try {
            hostUrl = new URL(HTTP_PROTOCOL, configuration.getHost(), configuration.getPort(), "");
        } catch (MalformedURLException e) {
            LOGGER.error("Error loading configuration: Invalid params for URL", e);
            throw new ConfigurationException("Invalid params to build URL");
        }

        generalPath = configuration.getGeneralPath();
        collectionPath = configuration.getCollectionPath();
        compressionEnabled = configuration.isCompressionEnabled();
        compressionThreshold = configuration.getCompressionThreshold();
    }

    @Override
    public void uplink(byte[] payload, ContentType contentType) {
        if (!isConfigured()) {
            LOGGER.error("HTTP connector is not configured");
            return;
        }

        String deviceId = deviceInfoProvider.getDeviceId();
        String apiKey = deviceInfoProvider.getApiKey();
        if (deviceId == null || apiKey == null) {
            LOGGER.error("Error sending HTTP message: Device info is not available to form URL");
            return;
        }

        URL url;
        try {
            url = getUrl();
        } catch (MalformedURLException e) {
            LOGGER.error("Error sending HTTP message: Invalid URL", e);
            return;
        }

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            EntityBuilder entityBuilder =
                    EntityBuilder.create().setBinary(payload).setContentType(getContentType(contentType));

            if (compressionEnabled && payload.length > compressionThreshold) {
                LOGGER.debug("Compressing data");
                entityBuilder.setContentEncoding(GZIP_ENCODING).gzipCompress();
            }

            HttpEntity entity = entityBuilder.build();

            HttpPost httpPost = new HttpPost(url.toString());
            httpPost.addHeader(API_KEY_HEADER_NAME, deviceInfoProvider.getApiKey());
            httpPost.setEntity(entity);

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (isSuccessCode(statusCode)) {
                LOGGER.debug("HTTP message sent");
            } else {
                String reason = httpResponse.getStatusLine().getReasonPhrase();
                LOGGER.error("Error sending HTTP message: {}, {}", statusCode, reason);
            }

            httpResponse.close();
        } catch (IOException exception) {
            LOGGER.error("Error sending HTTP message", exception);
        }
    }

    @Override
    public void uplinkNoQos(byte[] payload, ContentType contentType) {
        uplink(payload, contentType);
    }

    @Override
    public void uplinkResponse(byte[] payload, ContentType arg1) {
        LOGGER.error("Send response not suported");
    }

    private boolean isConfigured() {
        return hostUrl != null && generalPath != null && collectionPath != null;
    }

    private URL getUrl() throws MalformedURLException {
        return new URL(hostUrl, generalPath + "/" + deviceInfoProvider.getDeviceId() + collectionPath);
    }

    private org.apache.http.entity.ContentType getContentType(ContentType contentType) {
        return CONTENT_TYPE_MAPPER.get(contentType);
    }

    private boolean isSuccessCode(int statusCode) {
        return statusCode == OK_HTTP_CODE || statusCode == CREATED_HTTP_CODE;
    }

    @Override
    public boolean isConnected() {
        if (isConfigured()) {
            String host = "";
            try {
                host = hostUrl.getHost();
                InetAddress inetAddress = InetAddress.getByName(host);
                return inetAddress.isReachable(CONNECTION_TIMEOUT * MILLISECONDS_PER_SECOND);
            } catch (Exception e) {
                LOGGER.error("Error trying to reach \"{}\": {}", host, e.getMessage());
            }
        }
        return false;
    }

}
