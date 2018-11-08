package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocatorOsgi;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) {
        logger.info("Starting Operation RefreshInfo Activator");
        DatastreamsGettersLocator datastreamsGettersLocator = new DatastreamsGettersLocatorOsgi(context);
        registration = context.registerService(OperationRefreshInfo.class.getName(), new OperationRefreshInfoImpl(datastreamsGettersLocator), null);
        logger.info("Operation RefreshInfo started");
    }

    @Override
    public void stop(BundleContext context) {
        logger.info("Stopping Operation RefreshInfo Activator");
        registration.unregister();
        logger.info("Operation RefreshInfo stopped");
    }
}
