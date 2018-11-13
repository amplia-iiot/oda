package es.amplia.oda.connector.dnp3;

import es.amplia.oda.connector.dnp3.configuration.DNP3ConnectorConfigurationHandler;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableInfoProxy;
import es.amplia.oda.core.commons.utils.*;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    static {
        System.setProperty("com.automatak.dnp3.nostaticload", "");
        try {
            System.loadLibrary("openpal");
            System.loadLibrary("opendnp3");
            System.loadLibrary("asiopal");
            System.loadLibrary("asiodnp3");
            System.loadLibrary("opendnp3java");
        } catch (UnsatisfiedLinkError e) {
            logger.error("Error loading the opendnp3 native libs: {}", e.getMessage());
        }
    }

    private ScadaTableInfoProxy tableInfo;

    private ScadaDispatcherProxy dispatcher;

    private DNP3Connector connector;

    private DNP3ConnectorConfigurationHandler configHandler;

    private ConfigurableBundle configurableBundle;

    private ServiceListenerBundle<ScadaTableInfo> serviceListenerBundle;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        logger.info("DNP 3.0 connector is starting");
        tableInfo = new ScadaTableInfoProxy(bundleContext);
        dispatcher = new ScadaDispatcherProxy(bundleContext);
        ServiceRegistrationManager<ScadaConnector> scadaConnectorRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, ScadaConnector.class);
        connector = new DNP3Connector(tableInfo, dispatcher, scadaConnectorRegistrationManager);
        configHandler = new DNP3ConnectorConfigurationHandler(connector);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        serviceListenerBundle = new ServiceListenerBundle<>(bundleContext, ScadaTableInfo.class, this::onServiceChanged);

        logger.info("DNP 3.0 connector started");
    }

    void onServiceChanged() {
        try {
            configHandler.applyConfiguration();
        }catch (Exception exception) {
            logger.warn("Exception applying configuration");
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        logger.info("DNP 3.0 connector is stopping");

        serviceListenerBundle.close();
        connector.close();
        tableInfo.close();
        dispatcher.close();
        configurableBundle.close();

        logger.info("DNP 3.0 connector stopped");
    }
}
