package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.*;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.iec104.internal.Iec104DatastreamsFactoryImpl;

import es.amplia.oda.event.api.EventDispatcherProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private Iec104DatastreamsManager iec104DatastreamsManager;
    private ConfigurableBundle configurableBundle;
    private Iec104ConnectionsFactory connectionsFactory;
    private ServiceListenerBundle<ScadaTableInfo> serviceListenerBundle;
    private Iec104DatastreamsConfigurationUpdateHandler configHandler;
    private EventPublisherProxy eventPublisher;
    private EventDispatcherProxy eventDispatcher;


    @Override
    public void start(BundleContext bundleContext) {

        LOGGER.info("Starting up IEC104 datastreams bundle");

        eventPublisher = new EventPublisherProxy(bundleContext);
        eventDispatcher = new EventDispatcherProxy(bundleContext);

        serviceListenerBundle = new ServiceListenerBundle<>(bundleContext, ScadaTableInfo.class, this::onServiceChanged);
        ScadaTableTranslator translator = new ScadaTableTranslatorProxy(bundleContext);

        // initiate manager of IEC104 connections
        connectionsFactory = new Iec104ConnectionsFactory(eventDispatcher, eventPublisher, translator);

        // init data streams factory
        Iec104DatastreamsFactory iec104DatastreamsFactory = new Iec104DatastreamsFactoryImpl(translator, connectionsFactory);

        // create IEC104 data streams getter and setters
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);

        iec104DatastreamsManager =
                new Iec104DatastreamsManager(iec104DatastreamsFactory, datastreamsGetterRegistrationManager,
                        datastreamsSetterRegistrationManager, connectionsFactory, translator);

        // make bundle configurable
        configHandler = new Iec104DatastreamsConfigurationUpdateHandler(iec104DatastreamsManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

        LOGGER.info("IEC104 datastreams bundle started");
    }

    public void onServiceChanged() {
        configHandler.applyConfiguration();
    }

    @Override
    public void stop(BundleContext bundleContext) {

        LOGGER.info("Stopping IEC104IEC104 data streams bundle");

        configurableBundle.close();
        serviceListenerBundle.close();
        iec104DatastreamsManager.close();
        connectionsFactory.disconnect();
        eventPublisher.close();
        eventDispatcher.close();

        LOGGER.info("IEC104 data streams bundle stopped");
    }

}
