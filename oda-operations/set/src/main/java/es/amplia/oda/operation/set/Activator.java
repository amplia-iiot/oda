package es.amplia.oda.operation.set;

import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<?> registration;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Operation Set Activator");
        DatastreamsSettersLocator datastreamsSettersLocator = new DatastreamsSettersLocatorOsgi(context);
        DatastreamsSettersFinder datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
        OperationSetDeviceParametersImpl operationSetDeviceParameters = new OperationSetDeviceParametersImpl(datastreamsSettersFinder);
        registration = context.registerService(OperationSetDeviceParameters.class.getName(), operationSetDeviceParameters, null);
        logger.info("Operation Set Activator started");
    }

    @Override
    public void stop(BundleContext context) {
        logger.info("Stopping Operation Set Activator");
        registration.unregister();
        logger.info("Operation Set Activator stopped");
    }
}
