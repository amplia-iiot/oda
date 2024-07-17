package es.amplia.oda.datastreams.opcua;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.opcua.OpcUaConnection;
import es.amplia.oda.core.commons.osgi.proxies.OpcUaConnectionProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.opcua.internal.OpcUaDatastreamsFactoryImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private OpcUaDatastreamsManager opcUaDatastreamsManager;
    private ServiceListenerBundle<ScadaTableTranslator> serviceListenerBundle;
    private EventDispatcherProxy eventDispatcher;

    @Override
    public void start(BundleContext bundleContext) {

        LOGGER.info("Starting up OPC-UA datastreams bundle");

        eventDispatcher = new EventDispatcherProxy(bundleContext);

        serviceListenerBundle = new ServiceListenerBundle<>(bundleContext, ScadaTableTranslator.class, this::onServiceChanged);
        ScadaTableTranslator translator = new ScadaTableTranslatorProxy(bundleContext);

        // Get OPC-UA Connection Proxy
        OpcUaConnection connection = new OpcUaConnectionProxy(bundleContext);

        // init data streams factory
        OpcUaDatastreamsFactory opcUaDatastreamsFactory = new OpcUaDatastreamsFactoryImpl(translator, connection);

        // create IEC104 data streams getter and setters
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);

        opcUaDatastreamsManager =
                new OpcUaDatastreamsManager(opcUaDatastreamsFactory, datastreamsGetterRegistrationManager,
                        datastreamsSetterRegistrationManager, translator);

        LOGGER.info("OPC-UA datastreams bundle started");
    }

    public void onServiceChanged() {
        opcUaDatastreamsManager.updateDatastreams();
    }

    @Override
    public void stop(BundleContext bundleContext) {

        LOGGER.info("Stopping OPC-UA datastreams bundle");

        serviceListenerBundle.close();
        opcUaDatastreamsManager.close();
        eventDispatcher.close();

        LOGGER.info("OPC-UA datastreams bundle stopped");
    }

}
