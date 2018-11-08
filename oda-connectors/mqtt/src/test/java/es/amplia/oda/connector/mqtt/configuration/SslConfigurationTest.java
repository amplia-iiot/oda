package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SslConfiguration.class, SSLContext.class, KeyStore.class, URI.class, URL.class, KeyManagerFactory.class,
        TrustManagerFactory.class})
public class SslConfigurationTest {

    private static final String KEY_STORE_PATH = "path/to/keystore";
    private static final String KEY_STORE_TYPE = "jks";
    private static final String KEY_STORE_PASSWORD = "password";

    private SslConfiguration testSslConfiguration;

    @Before
    public void setUp() {
        testSslConfiguration = new SslConfiguration(KEY_STORE_PATH, KEY_STORE_TYPE, KEY_STORE_PASSWORD);
    }

    @Test
    public void testConstructor() {
        assertNotNull(testSslConfiguration);
    }

    @Test
    public void testConfigure() throws Exception {
        MqttConnectOptions mockedOptions = mock(MqttConnectOptions.class);
        PowerMockito.mockStatic(SSLContext.class);
        SSLContext mockedSSLContext = PowerMockito.mock(SSLContext.class);
        PowerMockito.mockStatic(KeyStore.class);
        KeyStore mockedKeyStore = PowerMockito.mock(KeyStore.class);
        File mockedFile = mock(File.class);
        URI mockedUri = PowerMockito.mock(URI.class);
        URL mockedUrl = PowerMockito.mock(URL.class);
        InputStream mockedStream = mock(InputStream.class);
        PowerMockito.mockStatic(KeyManagerFactory.class);
        KeyManagerFactory mockedKeyManagerFactory = PowerMockito.mock(KeyManagerFactory.class);
        PowerMockito.mockStatic(TrustManagerFactory.class);
        TrustManagerFactory mockedTrustManagerFactory = PowerMockito.mock(TrustManagerFactory.class);
        KeyManager[] mockedKeyManagers = {mock(KeyManager.class)};
        TrustManager[] mockedTrustManagers = {mock(TrustManager.class)};
        SSLSocketFactory mockedSocketFactory = mock(SSLSocketFactory.class);

        when(SSLContext.getInstance(eq("TLSv1.2"))).thenReturn(mockedSSLContext);
        when(KeyStore.getInstance(eq(KEY_STORE_TYPE))).thenReturn(mockedKeyStore);
        PowerMockito.whenNew(File.class)
                .withParameterTypes(String.class).withArguments(eq(KEY_STORE_PATH)).thenReturn(mockedFile);
        when(mockedFile.toURI()).thenReturn(mockedUri);
        when(mockedUri.toURL()).thenReturn(mockedUrl);
        when(mockedUrl.openStream()).thenReturn(mockedStream);
        when(KeyManagerFactory.getInstance(any())).thenReturn(mockedKeyManagerFactory);
        when(TrustManagerFactory.getInstance(any())).thenReturn(mockedTrustManagerFactory);
        when(mockedKeyManagerFactory.getKeyManagers()).thenReturn(mockedKeyManagers);
        when(mockedTrustManagerFactory.getTrustManagers()).thenReturn(mockedTrustManagers);
        when(mockedSSLContext.getSocketFactory()).thenReturn(mockedSocketFactory);

        testSslConfiguration.configure(mockedOptions);

        verify(mockedKeyStore).load(eq(mockedStream), aryEq(KEY_STORE_PASSWORD.toCharArray()));
        verify(mockedKeyManagerFactory).init(eq(mockedKeyStore), aryEq(KEY_STORE_PASSWORD.toCharArray()));
        verify(mockedTrustManagerFactory).init(eq(mockedKeyStore));
        verify(mockedSSLContext).init(eq(mockedKeyManagers), eq(mockedTrustManagers), any());
        verify(mockedOptions).setSocketFactory(eq(mockedSocketFactory));
    }

    @Test(expected = ConfigurationException.class)
    public void testConfigureGeneralSecurityException() throws KeyStoreException {
        MqttConnectOptions mockedOptions = mock(MqttConnectOptions.class);
        PowerMockito.mockStatic(KeyStore.class);

        when(KeyStore.getInstance(anyString())).thenThrow(new KeyStoreException());

        testSslConfiguration.configure(mockedOptions);

        fail("if GeneralSecurityException is throw must rethrow exception as ConfigurationException");
    }

    @Test(expected = ConfigurationException.class)
    public void testConfigureIOException() throws Exception {
        MqttConnectOptions mockedOptions = mock(MqttConnectOptions.class);
        PowerMockito.mockStatic(KeyStore.class);
        PowerMockito.mockStatic(KeyStore.class);
        KeyStore mockedKeyStore = PowerMockito.mock(KeyStore.class);
        File mockedFile = mock(File.class);
        URI mockedUri = PowerMockito.mock(URI.class);
        URL mockedUrl = PowerMockito.mock(URL.class);

        when(KeyStore.getInstance(eq(KEY_STORE_TYPE))).thenReturn(mockedKeyStore);
        PowerMockito.whenNew(File.class)
                .withParameterTypes(String.class).withArguments(eq(KEY_STORE_PATH)).thenReturn(mockedFile);
        when(mockedFile.toURI()).thenReturn(mockedUri);
        when(mockedUri.toURL()).thenReturn(mockedUrl);
        when(mockedUrl.openStream()).thenThrow(new IOException());

        testSslConfiguration.configure(mockedOptions);

        fail("if IOException is throw must rethrow exception as ConfigurationException");
    }
}