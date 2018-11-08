package es.amplia.oda.connector.thingstream;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.hardware.atserver.api.ATManagerProxy;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.osgi.proxies.DispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.connector.thingstream.configuration.ConfigurationUpdateHandlerImpl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ThingstreamConnector connector;
    private ATManagerProxy atManager;
    private DispatcherProxy dispatcher;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<ATManager> atManagerServiceListener;

    private ServiceRegistration<OpenGateConnector> openGateConnectorRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        logger.info("Starting Thingstream connector");

        atManager = new ATManagerProxy(bundleContext);
        dispatcher = new DispatcherProxy(bundleContext);
        connector = new ThingstreamConnector(atManager, dispatcher);

        ConfigurationUpdateHandler configHandler = new ConfigurationUpdateHandlerImpl(connector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        atManagerServiceListener = new ServiceListenerBundle<>(bundleContext, ATManager.class,
                () -> onServiceChanged(configHandler));

        openGateConnectorRegistration = bundleContext.registerService(OpenGateConnector.class, connector, null);

        logger.info("Thingstream connector started");
    }

    void onServiceChanged(ConfigurationUpdateHandler configHandler) {
        try {
            configHandler.applyConfiguration();
        }catch (Exception exception) {
            logger.warn("Exception applying es.amplia.oda.connector.thingstream.configuration");
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        logger.info("Stopping Thingstream connector");

        openGateConnectorRegistration.unregister();
        atManagerServiceListener.close();
        configurableBundle.close();
        connector.close();
        dispatcher.close();
        atManager.close();

        logger.info("Thingstream connector stopped");
    }
}
