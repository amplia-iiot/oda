package es.amplia.oda.comms.mqtt.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

import org.osgi.framework.BundleContext;

public class MqttClientFactoryProxy implements MqttClientFactory, AutoCloseable {

    private final OsgiServiceProxy<MqttClientFactory> proxy;

    public MqttClientFactoryProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(MqttClientFactory.class, bundleContext);
    }

    @Override
    public MqttClient createMqttClient(String serverUri, String clientId) {
        try {
            return proxy.callFirst(factory -> {
                try {
                    return factory.createMqttClient(serverUri, clientId);
                } catch (MqttException e) {
                    throw new MqttExceptionWrapper(e);
                }
            });
        } catch (MqttExceptionWrapper exceptionWrapper) {
            throw (MqttException) exceptionWrapper.getCause();
        }
    }

    static class MqttExceptionWrapper extends RuntimeException {
        MqttExceptionWrapper(MqttException innerException) {
            super(innerException);
        }
    }

    @Override
    public void close() {
        proxy.close();
    }
}
