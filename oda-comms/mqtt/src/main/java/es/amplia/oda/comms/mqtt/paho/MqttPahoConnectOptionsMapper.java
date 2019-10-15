package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;

import java.util.Optional;
import java.util.Properties;

import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.*;

class MqttPahoConnectOptionsMapper {

    static final String SYSKEYSTORE = "javax.net.ssl.keyStore";
    static final String SYSKEYSTORETYPE = "javax.net.ssl.keyStoreType";
    static final String SYSKEYSTOREPASS ="javax.net.ssl.keyStorePassword";
    static final String SYSTRUSTSTORE = "javax.net.ssl.trustStore";
    static final String SYSTRUSTSTORETYPE = "javax.net.ssl.trustStoreType";
    static final String SYSTRUSTSTOREPASS ="javax.net.ssl.trustStorePassword";
    static final String SYSKEYMGRALGO = "ssl.KeyManagerFactory.algorithm";
    static final String SYSTRUSTMGRALGO = "ssl.TrustManagerFactory.algorithm";

    // Non instantiable class
    private MqttPahoConnectOptionsMapper() {}

    static org.eclipse.paho.client.mqttv3.MqttConnectOptions from(MqttConnectOptions options) {
        org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions =
                new org.eclipse.paho.client.mqttv3.MqttConnectOptions();
        mapMqttVersionFrom(options, pahoOptions);
        pahoOptions.setUserName(options.getUsername());
        pahoOptions.setPassword(options.getPassword());
        pahoOptions.setKeepAliveInterval(options.getKeepAliveInterval());
        pahoOptions.setMaxInflight(options.getMaxInFlight());
        pahoOptions.setCleanSession(options.isCleanSession());
        pahoOptions.setConnectionTimeout(options.getConnectionTimeout());
        pahoOptions.setAutomaticReconnect(options.isAutomaticReconnect());
        mapWillOptionsFrom(options, pahoOptions);
        mapSslOptionsFrom(options, pahoOptions);
        return pahoOptions;
    }

    private static void mapMqttVersionFrom(MqttConnectOptions options,
                                           org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions) {
        MqttVersion mqttVersion = options.getMqttVersion();
        int pahoMqttVersion;
        switch (mqttVersion ) {
            case MQTT_3_1:
                pahoMqttVersion = org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1;
                break;
            case MQTT_3_1_1:
                pahoMqttVersion = org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1;
                break;
            default:
                throw new IllegalArgumentException("Invalid MQTT version: " + mqttVersion);
        }
        pahoOptions.setMqttVersion(pahoMqttVersion);
    }

    private static void mapWillOptionsFrom(MqttConnectOptions options,
                                           org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions) {
        Optional<WillOptions> willOptions = Optional.ofNullable(options.getWill());
        willOptions.ifPresent(will ->
                pahoOptions.setWill(will.getTopic(), will.getPayload(), will.getQos(), will.isRetained()));
    }

    private static void mapSslOptionsFrom(MqttConnectOptions options,
                                          org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions) {
        Optional<SslOptions> sslOptions = Optional.ofNullable(options.getSsl());
        sslOptions.ifPresent(ssl -> {
            Properties sslProperties = new Properties();
            sslProperties.put(SYSKEYSTORE, ssl.getKeyStore());
            sslProperties.put(SYSKEYSTORETYPE, ssl.getKeyStoreType());
            sslProperties.put(SYSKEYSTOREPASS, ssl.getKeyStorePassword());
            sslProperties.put(SYSKEYMGRALGO, ssl.getKeyManagerFactoryAlgorithm());
            sslProperties.put(SYSTRUSTSTORE, ssl.getTrustStore());
            sslProperties.put(SYSTRUSTSTORETYPE, ssl.getTrustStoreType());
            sslProperties.put(SYSTRUSTSTOREPASS, ssl.getTrustStorePassword());
            sslProperties.put(SYSTRUSTMGRALGO, ssl.getTrustManagerFactoryAlgorithm());
            pahoOptions.setSSLProperties(sslProperties);
        });
    }
}
