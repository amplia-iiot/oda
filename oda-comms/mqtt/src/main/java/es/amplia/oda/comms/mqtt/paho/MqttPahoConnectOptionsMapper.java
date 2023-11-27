package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.MqttVersion;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.SslOptions;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.WillOptions;

import java.util.Optional;
import java.util.Properties;

import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;

//import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.*;

class MqttPahoConnectOptionsMapper {

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
            sslProperties.put(SSLSocketFactoryFactory.KEYSTORE, ssl.getKeyStore());
            sslProperties.put(SSLSocketFactoryFactory.KEYSTORETYPE, ssl.getKeyStoreType());
            sslProperties.put(SSLSocketFactoryFactory.KEYSTOREPWD, ssl.getKeyStorePassword());
            //sslProperties.put(SYSKEYMGRALGO, ssl.getKeyManagerFactoryAlgorithm());
            sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, ssl.getTrustStore());
            sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, ssl.getTrustStoreType());
            sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, ssl.getTrustStorePassword());
            //sslProperties.put(SYSTRUSTMGRALGO, ssl.getTrustManagerFactoryAlgorithm());
            pahoOptions.setSSLProperties(sslProperties);
        });
    }
}
