package es.amplia.oda.comms.http;

import com.sun.net.httpserver.HttpServer;
import es.amplia.oda.comms.http.configuration.HttpClientConfiguration;
import es.amplia.oda.core.commons.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpClientImplTest {

    private static final String BODY = "ok";

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private HttpServer server;
    private String url;

    @Before
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] body = BODY.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @After
    public void stopServer() {
        server.stop(0);
    }

    @Test
    public void timeoutIsAppliedWithoutConfiguration() {
        HttpClientImpl client = new HttpClientImpl(null, false);

        assertNotNull(client.customConfig);
        assertEquals(HttpClientImpl.DEFAULT_TIMEOUT_MS, client.customConfig.getConnectTimeout());
        assertEquals(HttpClientImpl.DEFAULT_TIMEOUT_MS, client.customConfig.getSocketTimeout());
    }

    @Test
    public void timeoutIsAppliedInInsecureModeWithoutConfiguration() {
        HttpClientImpl client = new HttpClientImpl(null, true);

        assertEquals(HttpClientImpl.DEFAULT_TIMEOUT_MS, client.customConfig.getSocketTimeout());
    }

    @Test
    public void getWorksWithoutConfiguration() throws Exception {
        HttpResponse response = new HttpClientImpl(null, false).get(url, Collections.emptyMap());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void truststoreWithoutKeystoreBuildsSslClient() throws Exception {
        HttpClientConfiguration config = HttpClientConfiguration.builder()
                .trustStorePath(emptyStore("JKS", "secret").getAbsolutePath())
                .trustStoreType("JKS")
                .trustStorePassword("secret")
                .build();

        assertNotNull("keystore must be optional", new HttpClientImpl(null, false).httpClientSSL(config));
    }

    @Test
    public void wrongStorePathFallsBackToWorkingClient() throws Exception {
        HttpClientConfiguration config = HttpClientConfiguration.builder()
                .keyStorePath("/does/not/exist.jks")
                .keyStoreType("JKS")
                .keyStorePassword("secret")
                .build();

        HttpClientImpl client = new HttpClientImpl(config, false);
        assertNull(client.httpClientSSL(config));

        HttpResponse response = client.get(url, Collections.emptyMap());
        assertNotNull("a broken SSL config must not leave a null client", response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void compositeTrustsWhatJvmDefaultTrusts() throws Exception {
        X509TrustManager configured = mock(X509TrustManager.class);
        X509TrustManager jvmDefault = mock(X509TrustManager.class);
        X509Certificate[] chain = new X509Certificate[0];
        doThrow(new CertificateException("not in configured store"))
                .when(configured).checkServerTrusted(chain, "RSA");

        new HttpClientImpl.CompositeTrustManager(configured, jvmDefault).checkServerTrusted(chain, "RSA");

        verify(jvmDefault).checkServerTrusted(chain, "RSA");
    }

    @Test(expected = CertificateException.class)
    public void compositeRejectsWhenBothReject() throws Exception {
        X509TrustManager configured = mock(X509TrustManager.class);
        X509TrustManager jvmDefault = mock(X509TrustManager.class);
        X509Certificate[] chain = new X509Certificate[0];
        doThrow(new CertificateException("a")).when(configured).checkServerTrusted(chain, "RSA");
        doThrow(new CertificateException("b")).when(jvmDefault).checkServerTrusted(chain, "RSA");

        new HttpClientImpl.CompositeTrustManager(configured, jvmDefault).checkServerTrusted(chain, "RSA");
    }

    private File emptyStore(String type, String password) throws Exception {
        KeyStore store = KeyStore.getInstance(type);
        store.load(null, null);
        File file = tmp.newFile("store." + type.toLowerCase());
        try (FileOutputStream out = new FileOutputStream(file)) {
            store.store(out, password.toCharArray());
        }
        return file;
    }
}
