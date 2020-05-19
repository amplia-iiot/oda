package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.ruleengine.api.RuleEngineProxy;
import es.amplia.oda.statemanager.api.OsgiEventHandler;
import es.amplia.oda.statemanager.api.StateManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private DatastreamsSettersFinder datastreamsSettersFinder;
    private RuleEngineProxy ruleEngine;
    private EventDispatcherProxy eventDispatcher;
    private OsgiEventHandler eventHandler;
    private ServiceRegistration<StateManager> stateManagerRegistration;
    private SerializerProxy serializer;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("In Memory State Manager is starting");

        ServiceLocator<DatastreamsSetter> datastreamsSettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsSetter.class);
        datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        eventHandler = new OsgiEventHandler(bundleContext);
        ruleEngine = new RuleEngineProxy(bundleContext);
        serializer = new SerializerProxy(bundleContext, ContentType.JSON);
        StateManager inMemoryStateManager =
                new InMemoryStateManager(datastreamsSettersFinder, eventDispatcher, eventHandler, ruleEngine, serializer);
        stateManagerRegistration = bundleContext.registerService(StateManager.class, inMemoryStateManager, null);

        LOGGER.info("In Memory State Manager started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("In Memory State Manager is stopping");

        stateManagerRegistration.unregister();
        datastreamsSettersFinder.close();
        ruleEngine.close();
        eventHandler.close();
        eventDispatcher.close();

        LOGGER.info("In Memory State Manager is stopped");
    }
}
