package es.amplia.oda.comms.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.paho.MqttPahoClientFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<MqttClientFactory> mqttClientFactoryServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        mqttClientFactoryServiceRegistration =
                bundleContext.registerService(MqttClientFactory.class, new MqttPahoClientFactory(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) {
        mqttClientFactoryServiceRegistration.unregister();
    }
}
