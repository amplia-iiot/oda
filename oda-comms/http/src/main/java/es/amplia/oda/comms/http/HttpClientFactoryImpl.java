package es.amplia.oda.comms.http;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientFactoryImpl implements HttpClientFactory {

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
    
    @Override
    public HttpClient createClient() {
        return createClient(false);
    }

    @Override
    public HttpClient createClient(boolean insecure) {
        CloseableHttpClient httpClient;

        if (insecure) {
            httpClient = httpClientSSLNoVerify();
        } else {
            httpClient = HttpClients.createDefault();
        }

        return new HttpClient(httpClient);
    }

}
