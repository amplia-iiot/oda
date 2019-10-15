package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocatorOsgi;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


    private ServiceRegistration<OperationGetDeviceParameters> registration;


    @Override
    public void start(BundleContext context) {
        LOGGER.info("Starting Operation Get Activator");
        DatastreamsGettersLocator datastreamsGettersLocator = new DatastreamsGettersLocatorOsgi(context);
        DatastreamsGettersFinderImpl datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
        OperationGetDeviceParameters getter = new OperationGetDeviceParametersImpl(datastreamsGettersFinder);
        registration = context.registerService(OperationGetDeviceParameters.class, getter, null);
        LOGGER.info("Operation Get Activator started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping Operation Get Activator");
        registration.unregister();
        LOGGER.info("Operation Get Activator stopped");
    }
}
