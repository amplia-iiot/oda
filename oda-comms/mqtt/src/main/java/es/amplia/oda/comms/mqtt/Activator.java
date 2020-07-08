package es.amplia.oda.comms.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.paho.MqttPahoClientFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<MqttClientFactory> mqttClientFactoryServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting MQTT Comms bundle");
        mqttClientFactoryServiceRegistration =
                bundleContext.registerService(MqttClientFactory.class, new MqttPahoClientFactory(), null);
        LOGGER.info("MQTT Comms bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping MQTT Comms bundle");
        mqttClientFactoryServiceRegistration.unregister();
        LOGGER.info("MQTT Comms bundle stopped");
    }
}
