package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;

import es.amplia.oda.datastreams.simulator.configuration.SimulatedDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsSetterFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private SimulatedDatastreamsManager datastreamsManager;
    private ConfigurableBundle configurableBundle;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Datastreams Simulator bundle");

        SimulatedDatastreamsGetterFactory getterFactory = new SimulatedDatastreamsGetterFactory();
        SimulatedDatastreamsSetterFactory setterFactory = new SimulatedDatastreamsSetterFactory();
        ServiceRegistrationManager<DatastreamsGetter> registrationGetterManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> registrationSetterManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);
        datastreamsManager = new SimulatedDatastreamsManager(getterFactory, setterFactory,
                registrationGetterManager, registrationSetterManager);
        SimulatedDatastreamsConfigurationHandler configHandler =
                new SimulatedDatastreamsConfigurationHandler(datastreamsManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        LOGGER.info("Datastreams Simulator bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Datastreams Simulator bundle");

        configurableBundle.close();
        datastreamsManager.close();

        LOGGER.info("Datastreams Simulator bundle stopped");
    }
}
