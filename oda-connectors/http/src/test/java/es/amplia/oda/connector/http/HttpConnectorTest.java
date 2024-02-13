package es.amplia.oda.connector.http;

import es.amplia.oda.connector.http.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static es.amplia.oda.connector.http.HttpConnector.*;

import static es.amplia.oda.core.commons.entities.ContentType.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpConnector.class, HttpClientBuilder.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class HttpConnectorTest {

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 1234;
    private static final String TEST_GENERAL_PATH = "/general/path";
    private static final String TEST_COLLECTION_PATH = "/collection/path";
    private static final boolean TEST_COMPRESSION_ENABLED = true;
    private static final int TEST_COMPRESSION_THRESHOLD = 1024;
    private static final ConnectorConfiguration TEST_CONFIGURATION =
            ConnectorConfiguration.builder().host(TEST_HOST).port(TEST_PORT).generalPath(TEST_GENERAL_PATH)
                    .collectionPath(TEST_COLLECTION_PATH).compressionEnabled(TEST_COMPRESSION_ENABLED)
                    .compressionThreshold(TEST_COMPRESSION_THRESHOLD).build();
    private static final byte[] TEST_PAYLOAD = "Test message".getBytes(StandardCharsets.UTF_8);
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_API_KEY = "testApiKey";

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @InjectMocks
    private HttpConnector testConnector;

    @Test
    public void testLoadConfiguration() {
        testConnector.loadConfiguration(TEST_CONFIGURATION);

        URL hostUrl = (URL) Whitebox.getInternalState(testConnector, "hostUrl");
        assertEquals(HTTP_PROTOCOL, hostUrl.getProtocol());
        assertEquals(TEST_HOST, hostUrl.getHost());
        assertEquals(TEST_PORT, hostUrl.getPort());
        assertEquals(TEST_GENERAL_PATH, Whitebox.getInternalState(testConnector, "generalPath"));
        assertEquals(TEST_COLLECTION_PATH, Whitebox.getInternalState(testConnector, "collectionPath"));
        assertEquals(TEST_COMPRESSION_ENABLED, Whitebox.getInternalState(testConnector, "compressionEnabled"));
        assertEquals(TEST_COMPRESSION_THRESHOLD, Whitebox.getInternalState(testConnector, "compressionThreshold"));
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationInvalidUrl() {
        ConnectorConfiguration invalidConfiguration = ConnectorConfiguration.builder().host("host")
                .port(-50).generalPath("some/path").collectionPath("some/other/path").build();

        testConnector.loadConfiguration(invalidConfiguration);

        fail("Exception must be thrown");
    }

    @Test
    public void testUplinkHttpRequestResponseIsCreated() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(CREATED_HTTP_CODE);

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).execute(httpPostCaptor.capture());
        HttpPost httpPost = httpPostCaptor.getValue();
        assertEquals(testHostUrl.toString() + "/" + TEST_DEVICE_ID + TEST_COLLECTION_PATH, httpPost.getURI().toString());
        assertEquals(TEST_API_KEY, httpPost.getFirstHeader(API_KEY_HEADER_NAME).getValue());
        HttpEntity httpEntity = httpPost.getEntity();
        assertEquals(ContentType.APPLICATION_JSON.toString(), httpEntity.getContentType().getValue());
        assertNull(httpEntity.getContentEncoding());
        byte[] buffer = new byte[TEST_PAYLOAD.length];
        assertEquals(TEST_PAYLOAD.length, httpEntity.getContent().read(buffer));
        assertArrayEquals(TEST_PAYLOAD, buffer);
    }

    @Test
    public void testUplinkHttpRequestWithCborContentType() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(CREATED_HTTP_CODE);

        testConnector.uplink(TEST_PAYLOAD, CBOR);

        verify(mockedClient).execute(httpPostCaptor.capture());
        HttpPost httpPost = httpPostCaptor.getValue();
        assertEquals(testHostUrl.toString() + "/" + TEST_DEVICE_ID + TEST_COLLECTION_PATH, httpPost.getURI().toString());
        assertEquals(TEST_API_KEY, httpPost.getFirstHeader(API_KEY_HEADER_NAME).getValue());
        HttpEntity httpEntity = httpPost.getEntity();
        assertEquals(CBOR_MEDIA_TYPE, httpEntity.getContentType().getValue());
        assertNull(httpEntity.getContentEncoding());
        byte[] buffer = new byte[TEST_PAYLOAD.length];
        assertEquals(TEST_PAYLOAD.length, httpEntity.getContent().read(buffer));
        assertArrayEquals(TEST_PAYLOAD, buffer);
    }

    @Test
    public void testUplinkHttpRequestWithMessagePackContentType() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(CREATED_HTTP_CODE);

        testConnector.uplink(TEST_PAYLOAD, MESSAGE_PACK);

        verify(mockedClient).execute(httpPostCaptor.capture());
        HttpPost httpPost = httpPostCaptor.getValue();
        assertEquals(testHostUrl.toString() + "/" + TEST_DEVICE_ID + TEST_COLLECTION_PATH, httpPost.getURI().toString());
        assertEquals(TEST_API_KEY, httpPost.getFirstHeader(API_KEY_HEADER_NAME).getValue());
        HttpEntity httpEntity = httpPost.getEntity();
        assertEquals(UNOFFICIAL_MESSAGE_PACK_MEDIA_TYPE, httpEntity.getContentType().getValue());
        assertNull(httpEntity.getContentEncoding());
        byte[] buffer = new byte[TEST_PAYLOAD.length];
        assertEquals(TEST_PAYLOAD.length, httpEntity.getContent().read(buffer));
        assertArrayEquals(TEST_PAYLOAD, buffer);
    }

    @Test
    public void testUplinkHttpRequestResponseIsOk() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(OK_HTTP_CODE);

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).execute(httpPostCaptor.capture());
        HttpPost httpPost = httpPostCaptor.getValue();
        assertEquals(testHostUrl.toString() + "/" + TEST_DEVICE_ID + TEST_COLLECTION_PATH, httpPost.getURI().toString());
        assertEquals(TEST_API_KEY, httpPost.getFirstHeader(API_KEY_HEADER_NAME).getValue());
        HttpEntity httpEntity = httpPost.getEntity();
        assertEquals(ContentType.APPLICATION_JSON.toString(), httpEntity.getContentType().getValue());
        assertNull(httpEntity.getContentEncoding());
        byte[] buffer = new byte[TEST_PAYLOAD.length];
        assertEquals(TEST_PAYLOAD.length, httpEntity.getContent().read(buffer));
        assertArrayEquals(TEST_PAYLOAD, buffer);
    }

    @Test
    public void testUplinkCompressionEnabledAndThresholdExceeded() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);
        Whitebox.setInternalState(testConnector, "compressionEnabled", TEST_COMPRESSION_ENABLED);
        Whitebox.setInternalState(testConnector, "compressionThreshold", 1);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(OK_HTTP_CODE);

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).execute(httpPostCaptor.capture());
        HttpPost httpPost = httpPostCaptor.getValue();
        assertEquals(testHostUrl.toString() + "/" + TEST_DEVICE_ID + TEST_COLLECTION_PATH, httpPost.getURI().toString());
        assertEquals(TEST_API_KEY, httpPost.getFirstHeader(API_KEY_HEADER_NAME).getValue());
        HttpEntity httpEntity = httpPost.getEntity();
        assertEquals(ContentType.APPLICATION_JSON.toString(), httpEntity.getContentType().getValue());
        assertEquals(GZIP_ENCODING, httpEntity.getContentEncoding().getValue());
    }

    @Test
    public void testUplinkCompressionEnabledAndThresholdIsNotExceeded() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);
        Whitebox.setInternalState(testConnector, "compressionEnabled", TEST_COMPRESSION_ENABLED);
        Whitebox.setInternalState(testConnector, "compressionThreshold", 1024);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(OK_HTTP_CODE);

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).execute(httpPostCaptor.capture());
        HttpPost httpPost = httpPostCaptor.getValue();
        assertEquals(testHostUrl.toString() + "/" + TEST_DEVICE_ID + TEST_COLLECTION_PATH, httpPost.getURI().toString());
        assertEquals(TEST_API_KEY, httpPost.getFirstHeader(API_KEY_HEADER_NAME).getValue());
        HttpEntity httpEntity = httpPost.getEntity();
        assertEquals(ContentType.APPLICATION_JSON.toString(), httpEntity.getContentType().getValue());
        assertNull(httpEntity.getContentEncoding());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testUplinkConnectorNoConfigured() {
        Whitebox.setInternalState(testConnector, "hostUrl", null);

        PowerMockito.mockStatic(HttpClientBuilder.class);

        testConnector.uplink(TEST_PAYLOAD);

        PowerMockito.verifyStatic(HttpClientBuilder.class, never());
        HttpClientBuilder.create();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testUplinkNoDeviceId() throws MalformedURLException {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);

        testConnector.uplink(TEST_PAYLOAD);

        PowerMockito.verifyStatic(HttpClientBuilder.class, never());
        HttpClientBuilder.create();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testUplinkNoApiKey() throws MalformedURLException {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);
        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(null);

        testConnector.uplink(TEST_PAYLOAD);

        PowerMockito.verifyStatic(HttpClientBuilder.class, never());
        HttpClientBuilder.create();
    }

    @Test
    public void testUplinkHttpRequestResponseIsAFailureCode() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedHttpResponse = mock(CloseableHttpResponse.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);
        Whitebox.setInternalState(testConnector, "compressionEnabled", TEST_COMPRESSION_ENABLED);
        Whitebox.setInternalState(testConnector, "compressionThreshold", 1024);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenReturn(mockedHttpResponse);
        when(mockedHttpResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(500);
        when(mockedStatusLine.getReasonPhrase()).thenReturn("Error");

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).execute(any(HttpPost.class));
    }

    @Test
    public void testUplinkHttpClientExceptionCaught() throws Exception {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        HttpClientBuilder mockedClientBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);
        Whitebox.setInternalState(testConnector, "compressionEnabled", TEST_COMPRESSION_ENABLED);
        Whitebox.setInternalState(testConnector, "compressionThreshold", 1024);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedClientBuilder);
        when(mockedClientBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpPost.class))).thenThrow(new IOException());

        testConnector.uplink(TEST_PAYLOAD);

        verify(mockedClient).execute(any(HttpPost.class));
        assertTrue("Exception is caught", true);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testIsConnected() throws IOException {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        InetAddress mockedAddress = mock(InetAddress.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);

        PowerMockito.mockStatic(InetAddress.class);
        when(InetAddress.getByName(anyString())).thenReturn(mockedAddress);
        when(mockedAddress.isReachable(anyInt())).thenReturn(true);

        boolean connected = testConnector.isConnected();

        assertTrue(connected);
        PowerMockito.verifyStatic(InetAddress.class);
        InetAddress.getByName(TEST_HOST);
        verify(mockedAddress).isReachable(eq(CONNECTION_TIMEOUT * MILLISECONDS_PER_SECOND));
    }

    @Test
    public void testIsConnectedNoHostUrlConfigured() {
        Whitebox.setInternalState(testConnector, "hostUrl", null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedNoGeneralPathConfigured() throws MalformedURLException {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedNoCollectionPathConfigured() throws MalformedURLException {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testIsConnectedInetAddressException() throws IOException {
        URL testHostUrl = new URL(HTTP_PROTOCOL, TEST_HOST, TEST_PORT, TEST_GENERAL_PATH);
        InetAddress mockedAddress = mock(InetAddress.class);

        Whitebox.setInternalState(testConnector, "hostUrl", testHostUrl);
        Whitebox.setInternalState(testConnector, "generalPath", TEST_GENERAL_PATH);
        Whitebox.setInternalState(testConnector, "collectionPath", TEST_COLLECTION_PATH);

        PowerMockito.mockStatic(InetAddress.class);
        when(InetAddress.getByName(anyString())).thenReturn(mockedAddress);
        when(mockedAddress.isReachable(anyInt())).thenThrow(new IOException());

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
        PowerMockito.verifyStatic(InetAddress.class);
        InetAddress.getByName(TEST_HOST);
        verify(mockedAddress).isReachable(eq(CONNECTION_TIMEOUT * MILLISECONDS_PER_SECOND));
    }
}