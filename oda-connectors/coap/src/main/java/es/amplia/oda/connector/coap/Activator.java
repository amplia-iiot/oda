package es.amplia.oda.connector.coap;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atserver.api.ATManagerProxy;
import es.amplia.oda.connector.coap.configuration.ConfigurationUpdateHandlerImpl;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private DeviceInfoProviderProxy deviceInfoProvider;
    private ATManagerProxy atManager;
    private COAPConnector connector;
    private ConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoServiceListener;
    private ServiceListenerBundle<ATManager> atManagerServiceListener;

    private ServiceRegistration<OpenGateConnector> registration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting COAP connector");

        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        atManager = new ATManagerProxy(bundleContext);
        COAPClientFactory coapClientFactory = new COAPClientFactory(deviceInfoProvider, atManager);
        connector = new COAPConnector(coapClientFactory);
        configHandler = new ConfigurationUpdateHandlerImpl(connector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        deviceInfoServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
                this::onServiceChanged);
        atManagerServiceListener = new ServiceListenerBundle<>(bundleContext, ATManager.class, this::onServiceChanged);

        registration = bundleContext.registerService(OpenGateConnector.class, connector, null);

        LOGGER.info("COAP connector started");
    }

    void onServiceChanged() {
        LOGGER.info("Device Info provider or AT Manager service changed. Applying COAP connector configuration");
        try {
            configHandler.applyConfiguration();
        } catch (Exception e) {
            LOGGER.error("Error applying configuration", e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping COAP connector");

        registration.unregister();
        deviceInfoServiceListener.close();
        atManagerServiceListener.close();
        atManager.close();
        configurableBundle.close();
        connector.close();
        deviceInfoProvider.close();

        LOGGER.info("COAP connector stopped");
    }
}
