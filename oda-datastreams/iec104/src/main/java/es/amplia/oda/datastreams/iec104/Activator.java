package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.iec104.internal.Iec104DatastreamsFactoryImpl;

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

    @Override
    public void start(BundleContext bundleContext) {

        LOGGER.info("Starting up IEC104 datastreams bundle");

        // initiate manager of IEC104 connections
        connectionsFactory = new Iec104ConnectionsFactory();

        serviceListenerBundle = new ServiceListenerBundle<>(bundleContext, ScadaTableInfo.class, this::onServiceChanged);
        ScadaTableTranslator translator = new ScadaTableTranslatorProxy(bundleContext);

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
        connectionsFactory.disconnect();
        connectionsFactory.deleteConnections(); // Eliminamos todas las conexiones antiguas antes de aplicara la nueva configuración
        configHandler.applyConfiguration();
        connectionsFactory.connect();
    }

    @Override
    public void stop(BundleContext bundleContext) {

        LOGGER.info("Stopping IEC104IEC104 data streams bundle");

        configurableBundle.close();
        serviceListenerBundle.close();
        iec104DatastreamsManager.close();
        connectionsFactory.disconnect();

        LOGGER.info("IEC104 data streams bundle stopped");
    }

}
