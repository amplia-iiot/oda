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

/**
 * SSL configuration.
 */
public class SslConfiguration implements MqttConfiguration {

    /**
     * Key store path.
     */
    private final String keyStorePath;

    /**
     * Key store type.
     */
    private final String keyStoreType;

    /**
     * Key store password.
     */
    private final String keyStorePassword;

    /**
     * Constructor.
     *
     * @param keyStorePath     Key store path.
     * @param keyStoreType     Key store type.
     * @param keyStorePassword Key store password.
     */
    SslConfiguration(String keyStorePath, String keyStoreType, String keyStorePassword) {
        this.keyStorePath = keyStorePath;
        this.keyStoreType = keyStoreType;
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Configure the Mqtt Connect Options with the SSL configuration.
     *
     * @param options options to configure the SSL.
     * @throws ConfigurationException Exception configuring SSL.
     */
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

    /**
     * Read the key store with the object information.
     *
     * @return Specified key store.
     * @throws GeneralSecurityException The key store integrity cannot be or any certificate in the key store can not be loaded.
     * @throws IOException              Key Store file not exists or there is a format problem with the key store data.
     */
    private KeyStore readKeyStore() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        URL url = new File(keyStorePath).toURI().toURL();
        keyStore.load(url.openStream(), keyStorePassword.toCharArray());
        return keyStore;
    }
}
