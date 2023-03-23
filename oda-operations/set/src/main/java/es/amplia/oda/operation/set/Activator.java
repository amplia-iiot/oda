package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private StateManagerProxy stateManager;
    private ServiceRegistration<OperationSetDeviceParameters> registration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Operation Set Activator");
        stateManager = new StateManagerProxy(bundleContext);
        OperationSetDeviceParametersImpl setDeviceParameters = new OperationSetDeviceParametersImpl(stateManager);
        registration = bundleContext.registerService(OperationSetDeviceParameters.class, setDeviceParameters, null);
        LOGGER.info("Operation Set Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Operation Set Activator");
        registration.unregister();
        stateManager.close();
        LOGGER.info("Operation Set Activator stopped");
    }
}
