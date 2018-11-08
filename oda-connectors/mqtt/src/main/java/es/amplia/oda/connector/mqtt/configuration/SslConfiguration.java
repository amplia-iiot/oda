package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

public class SslConfiguration implements MqttConfiguration {

    private final String keyStorePath;
    private final String keyStoreType;
    private final String keyStorePassword;

    SslConfiguration(String keyStorePath, String keyStoreType, String keyStorePassword) {
        this.keyStorePath = keyStorePath;
        this.keyStoreType = keyStoreType;
        this.keyStorePassword = keyStorePassword;
    }

    public void configure(MqttConnectOptions options) throws ConfigurationException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

            KeyStore keyStore = readKeyStore();
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            options.setSocketFactory(sslContext.getSocketFactory());
        } catch (IOException | GeneralSecurityException exception) {
            throw new ConfigurationException("Error configuring SSL: " + exception);
        }
    }

    private KeyStore readKeyStore() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        URL url = new File(keyStorePath).toURI().toURL();
        keyStore.load(url.openStream(), keyStorePassword.toCharArray());
        return keyStore;
    }
}
