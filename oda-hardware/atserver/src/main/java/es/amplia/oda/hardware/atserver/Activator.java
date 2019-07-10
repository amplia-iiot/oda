package es.amplia.oda.hardware.atserver;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.atmanager.api.ATManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ATServer atServer;
    private ConfigurableBundle configurableBundle;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("AT Server starting");

        ServiceRegistrationManager<ATManager> atManagerRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, ATManager.class);
        atServer = new ATServer(atManagerRegistrationManager);
        ATServerConfigurationUpdateHandler configHandler = new ATServerConfigurationUpdateHandler(atServer);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        LOGGER.info("AT Server started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("AT Server stopping");

        configurableBundle.close();
        atServer.close();

        LOGGER.info("AT Server stopped");
    }
}
