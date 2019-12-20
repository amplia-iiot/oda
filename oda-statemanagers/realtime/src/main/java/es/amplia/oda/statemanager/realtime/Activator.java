package es.amplia.oda.statemanager.realtime;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.statemanager.api.EventHandler;
import es.amplia.oda.statemanager.api.OsgiEventHandler;
import es.amplia.oda.statemanager.api.StateManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


public class Activator implements BundleActivator {

    private DatastreamsGettersFinder datastreamsGettersFinder;
    private DatastreamsSettersFinder datastreamsSettersFinder;
    private EventDispatcherProxy eventDispatcher;
    private EventHandler eventHandler;
    private ServiceRegistration<StateManager> registration;

    @Override
    public void start(BundleContext bundleContext) {
        ServiceLocator<DatastreamsGetter> datastreamsGettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsGetter.class);
        datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
        ServiceLocator<DatastreamsSetter> datastreamsSettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsSetter.class);
        datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        eventHandler = new OsgiEventHandler(bundleContext);
        StateManager stateManager =
                new RealTimeStateManager(datastreamsGettersFinder, datastreamsSettersFinder, eventHandler, eventDispatcher);
        registration = bundleContext.registerService(StateManager.class, stateManager, null);

    }

    @Override
    public void stop(BundleContext bundleContext) {
        registration.unregister();
        datastreamsGettersFinder.close();
        datastreamsSettersFinder.close();
        eventHandler.close();
        eventDispatcher.close();
    }
}
