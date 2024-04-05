package es.amplia.oda.comms.mqtt.api;

import lombok.*;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import java.util.Objects;

@Value
public class MqttConnectOptions {

    public static final MqttVersion DEFAULT_MQTT_VERSION = MqttVersion.MQTT_3_1_1;
    public static final int DEFAULT_KEEP_ALIVE_INTERVAL = 60;
    public static final int DEFAULT_MAX_IN_FLIGHT = 10;
    public static final boolean DEFAULT_CLEAN_SESSION = true;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30;
    public static final boolean DEFAULT_AUTOMATIC_RECONNECT = true;

    MqttVersion mqttVersion;
    String username;
    char[] password;
    int keepAliveInterval;
    int maxInFlight;
    boolean cleanSession;
    int connectionTimeout;
    boolean automaticReconnect;
    WillOptions will;
    SslOptions ssl;

    public enum MqttVersion {
        MQTT_3_1, MQTT_3_1_1
    }

    public enum KeyStoreType {
        JKS, JCEKS, PKCS12, PKCS11, DKS, WINDOWS_MY, BKS
    }

    public enum KeyManagerAlgorithm {
        PKIX, SUN_X509, SUN_JSSE;

        public static KeyManagerAlgorithm from(String algorithmName) {
            if ("PKIX".equals(algorithmName)) {
                return KeyManagerAlgorithm.PKIX;
            } else if ("SunX509".equals(algorithmName)) {
                return KeyManagerAlgorithm.SUN_X509;
            } else if ("SunJSSE".equals(algorithmName)) {
                return KeyManagerAlgorithm.SUN_JSSE;
            } else {
                try {
                    return KeyManagerAlgorithm.valueOf(algorithmName);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown algorithm name: " + algorithmName, e);
                }

            }
        }
    }

    @Value
    public static class WillOptions {
        public static final int DEFAULT_QOS = 1;
        public static final boolean DEFAULT_RETAINED = false;

        String topic;
        byte[] payload;
        int qos;
        boolean retained;

        private WillOptions(String topic, byte[] payload, int qos, boolean retained) {
            this.topic = topic;
            this.payload = payload.clone();
            this.qos = qos;
            this.retained = retained;
        }

        public byte[] getPayload() {
            return payload.clone();
        }
    }

    @Value
    public static class SslOptions {

        public static final KeyStoreType DEFAULT_KEY_STORE_TYPE = KeyStoreType.JKS;
        public static final KeyManagerAlgorithm DEFAULT_KEY_MANAGER_ALGORITHM =
               KeyManagerAlgorithm.from(KeyManagerFactory.getDefaultAlgorithm());

        String keyStore;
        KeyStoreType keyStoreType;
        char[] keyStorePassword;
        KeyManagerAlgorithm keyManagerFactoryAlgorithm;
        String trustStore;
        KeyStoreType trustStoreType;
        char[] trustStorePassword;
        KeyManagerAlgorithm trustManagerFactoryAlgorithm;
        SocketFactory sslSocketFactory;

        SslOptions(String keyStore, KeyStoreType keyStoreType, char[] keyStorePassword,
                   KeyManagerAlgorithm keyManagerFactoryAlgorithm, String trustStore, KeyStoreType trustStoreType,
                   char[] trustStorePassword, KeyManagerAlgorithm trustManagerFactoryAlgorithm,
                   SocketFactory socketFactory) {
            this.keyStore = keyStore;
            this.keyStoreType = keyStoreType;
            this.keyStorePassword = keyStorePassword!=null ? keyStorePassword.clone() : null;
            this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
            this.trustStore = trustStore;
            this.trustStoreType = trustStoreType;
            this.trustStorePassword = trustStorePassword!= null ? trustStorePassword.clone() : null;
            this.trustManagerFactoryAlgorithm = trustManagerFactoryAlgorithm;
            this.sslSocketFactory = socketFactory;
        }

        public char[] getTrustStorePassword() {
            return trustStorePassword!= null ? trustStorePassword.clone() : null;
        }
    }

    // Private constructor to force the use of builder instead
    private MqttConnectOptions(MqttVersion mqttVersion, String username, char[] password, int keepAliveInterval,
                              int maxInFlight, boolean cleanSession, int connectionTimeout, boolean automaticReconnect,
                              WillOptions will, SslOptions ssl) {
        this.mqttVersion = mqttVersion;
        this.username = username;
        this.password = password.clone();
        this.keepAliveInterval = keepAliveInterval;
        this.maxInFlight = maxInFlight;
        this.cleanSession = cleanSession;
        this.connectionTimeout = connectionTimeout;
        this.automaticReconnect = automaticReconnect;
        this.will = will;
        this.ssl = ssl;
    }

    public char[] getPassword() {
        return password.clone();
    }

    public static MqttConnectOptionsBuilder builder(String username, char[] password) {
        return new MqttConnectOptionsBuilder(username, password);
    }

    public static class MqttConnectOptionsBuilder {

        private MqttVersion mqttVersion = DEFAULT_MQTT_VERSION;
        private final String username;
        private final char[] password;
        private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;
        private int maxInFlight = DEFAULT_MAX_IN_FLIGHT;
        private boolean cleanSession = DEFAULT_CLEAN_SESSION;
        private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        private boolean automaticReconnect = DEFAULT_AUTOMATIC_RECONNECT;
        private WillOptions will = null;
        private SslOptions ssl = null;

        private MqttConnectOptionsBuilder(String username, char[] password) {
            this.username = username;
            this.password = password;
        }

        public MqttConnectOptionsBuilder mqttVersion(MqttVersion mqttVersion) {
            this.mqttVersion = mqttVersion;
            return this;
        }

        public MqttConnectOptionsBuilder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public MqttConnectOptionsBuilder maxInFlight(int maxInFlight) {
            this.maxInFlight = maxInFlight;
            return this;
        }

        public MqttConnectOptionsBuilder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public MqttConnectOptionsBuilder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public MqttConnectOptionsBuilder automaticReconnect(boolean automaticReconnect) {
            this.automaticReconnect = automaticReconnect;
            return this;
        }

        public MqttConnectOptionsBuilder will(String topic, byte[] payload) {
            return will(topic, payload, WillOptions.DEFAULT_QOS, WillOptions.DEFAULT_RETAINED);
        }

        public MqttConnectOptionsBuilder will(String topic, byte[] payload, int qos, boolean retained) {
            this.will = new WillOptions(topic, payload, qos, retained);
            return this;
        }

        public MqttConnectOptionsBuilder ssl(String keyStore, char[] keyStorePassword, String trustStore,
                                             char[] trustStorePassword) {
            return ssl(keyStore, SslOptions.DEFAULT_KEY_STORE_TYPE, keyStorePassword,
                    SslOptions.DEFAULT_KEY_MANAGER_ALGORITHM, trustStore, SslOptions.DEFAULT_KEY_STORE_TYPE,
                    trustStorePassword, SslOptions.DEFAULT_KEY_MANAGER_ALGORITHM);
        }

        public MqttConnectOptionsBuilder ssl(String keyStore, KeyStoreType keyStoreType, char[] keyStorePassword,
                                             KeyManagerAlgorithm keyManagerAlgorithm, String trustStore,
                                             KeyStoreType trustStoreType, char[] trustStorePassword,
                                             KeyManagerAlgorithm trustManagerAlgorithm) {
            Objects.requireNonNull(keyStorePassword, "Key Store password must not be null");
            Objects.requireNonNull(trustStorePassword, "Trust Store password must not be null");
            this.ssl = new SslOptions(keyStore, keyStoreType, keyStorePassword, keyManagerAlgorithm, trustStore,
                    trustStoreType, trustStorePassword, trustManagerAlgorithm, null);
            return this;
        }

        public MqttConnectOptionsBuilder ssl(SocketFactory socketFactory) {
            this.ssl = new SslOptions(null, null, null,
                    null, null, null, null,
                    null, socketFactory);
            return this;
        }

        public MqttConnectOptions build() {
            return new MqttConnectOptions(mqttVersion, username, password, keepAliveInterval, maxInFlight,
                    cleanSession, connectionTimeout, automaticReconnect, will, ssl);
        }
    }
}
