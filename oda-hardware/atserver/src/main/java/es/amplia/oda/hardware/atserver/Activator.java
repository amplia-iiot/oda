package es.amplia.oda.hardware.atserver;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.atmanager.api.ATManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ATServer atServer;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<ATManager> atManagerRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("AT Server starting");

        atServer = new ATServer();
        atManagerRegistration = bundleContext.registerService(ATManager.class, atServer, null);
        ATServerConfigurationUpdateHandler configHandler = new ATServerConfigurationUpdateHandler(atServer);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
                Collections.singletonList(atManagerRegistration));

        LOGGER.info("AT Server started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("AT Server stopping");

        atManagerRegistration.unregister();
        configurableBundle.close();
        atServer.close();

        LOGGER.info("AT Server stopped");
    }
}
