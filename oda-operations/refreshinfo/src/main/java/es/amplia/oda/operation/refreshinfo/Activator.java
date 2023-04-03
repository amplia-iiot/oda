package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private StateManagerProxy stateManager;
    private ServiceRegistration<OperationRefreshInfo> registration;

    @Override
    public void start(BundleContext context) {
        LOGGER.info("Starting Operation RefreshInfo Activator");
        stateManager = new StateManagerProxy(context);
        OperationRefreshInfoImpl refreshInfo = new OperationRefreshInfoImpl(stateManager);
        registration = context.registerService(OperationRefreshInfo.class, refreshInfo, null);
        LOGGER.info("Operation RefreshInfo started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping Operation RefreshInfo Activator");
        registration.unregister();
        stateManager.close();
        LOGGER.info("Operation RefreshInfo stopped");
    }
}
