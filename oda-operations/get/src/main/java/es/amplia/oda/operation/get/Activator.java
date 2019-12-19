package es.amplia.oda.operation.get;

import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.statemanager.api.StateManagerProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


    private StateManagerProxy stateManager;
    private ServiceRegistration<OperationGetDeviceParameters> registration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Operation Get Activator");

        stateManager = new StateManagerProxy(bundleContext);
        OperationGetDeviceParameters getter = new OperationGetDeviceParametersImpl(stateManager);
        registration = bundleContext.registerService(OperationGetDeviceParameters.class, getter, null);

        LOGGER.info("Operation Get Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Operation Get Activator");

        registration.unregister();
        stateManager.close();

        LOGGER.info("Operation Get Activator stopped");
    }
}
