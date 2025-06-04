package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinderImpl;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);


    private StateManagerProxy stateManager;
    private ServiceRegistration<OperationGetDeviceParameters> registration;
    private DatastreamsGettersFinder datastreamsGettersFinder;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Operation Get Activator");

        stateManager = new StateManagerProxy(bundleContext);
        ServiceLocator<DatastreamsGetter> datastreamsGettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsGetter.class);
        datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
        OperationGetDeviceParameters getter = new OperationGetDeviceParametersImpl(stateManager, datastreamsGettersFinder);
        registration = bundleContext.registerService(OperationGetDeviceParameters.class, getter, null);

        LOGGER.info("Operation Get Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Operation Get Activator");

        registration.unregister();
        stateManager.close();
        datastreamsGettersFinder.close();

        LOGGER.info("Operation Get Activator stopped");
    }
}
