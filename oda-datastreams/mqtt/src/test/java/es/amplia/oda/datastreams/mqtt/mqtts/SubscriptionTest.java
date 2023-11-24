package es.amplia.oda.datastreams.mqtt.mqtts;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

public class SubscriptionTest {
    //@Test
    public void main() throws Exception {
        MqttClient client = new MqttClient("ssl://localhost:8883", "jj_device", new MemoryPersistence());

        //CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        // load CA certificate into keystore to authenticate server
        //X509Certificate x509CaCert = (X509Certificate) certFactory.generateCertificate(new FileInputStream("C:\\Users\\Juanjo\\Downloads\\mosquitto\\certGeneratorA\\ca.crt"));

        //KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        //caKeyStore.load(new FileInputStream("C:\\Users\\Juanjo\\Downloads\\mosquitto\\certGeneratorA\\keystore.jks"), new char[]{'j', 'j', '_', 'p', 'a', 's', 's'});
        /*caKeyStore.load(null, null);
        caKeyStore.setCertificateEntry("cacert", x509CaCert);*/

        //TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        //tmFactory.init(caKeyStore);

        //SSLContext sslContext = SSLContext.getInstance("TLS");
        //sslContext.init(null, tmFactory.getTrustManagers(), null);

        //SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setPassword("jj_pass".toCharArray());
        options.setUserName("jj_device");
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        Properties sslProperties = new Properties();
        sslProperties.put(SSLSocketFactoryFactory.KEYSTORE, "C:\\Users\\Juanjo\\Downloads\\mosquitto\\certGeneratorA\\keystore.jks");
        sslProperties.put(SSLSocketFactoryFactory.KEYSTORETYPE, "JKS");
        sslProperties.put(SSLSocketFactoryFactory.KEYSTOREPWD, new char[]{'j', 'j', '_', 'p', 'a', 's', 's'});
        sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORE, "C:\\Users\\Juanjo\\Downloads\\mosquitto\\certGeneratorA\\keystore.jks");
        sslProperties.put(SSLSocketFactoryFactory.TRUSTSTORETYPE, "JKS");
        sslProperties.put(SSLSocketFactoryFactory.TRUSTSTOREPWD, new char[]{'j', 'j', '_', 'p', 'a', 's', 's'});
        options.setSSLProperties(sslProperties);
        //options.setSocketFactory(sslSocketFactory);


        client.connect(options);
        client.subscribe("odm/iot/jj_device", new IMqttMessageListener() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("On topic: " + topic + ", received message: " + message);
            }
            
        });
    }
}
