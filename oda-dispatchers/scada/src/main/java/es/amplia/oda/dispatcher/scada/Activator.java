package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.osgi.proxies.ScadaConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.operation.api.osgi.proxies.OperationGetDeviceParametersProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationSetDeviceParametersProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ScadaTableTranslatorProxy translator;
    private OperationGetDeviceParametersProxy getOperation;
    private OperationSetDeviceParametersProxy setOperation;
    private ScadaConnectorProxy connector;

    private ServiceRegistration<ScadaDispatcher> operationDispatcherRegistration;
    private ServiceRegistration<EventDispatcher> eventDispatcherRegistration;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting SCADA dispatcher");

        translator = new ScadaTableTranslatorProxy(bundleContext);
        getOperation = new OperationGetDeviceParametersProxy(bundleContext);
        setOperation = new OperationSetDeviceParametersProxy(bundleContext);
        ScadaOperationDispatcher operationDispatcher =
                new ScadaOperationDispatcher(translator, getOperation, setOperation);
        operationDispatcherRegistration =
                bundleContext.registerService(ScadaDispatcher.class, operationDispatcher, null);
        connector = new ScadaConnectorProxy(bundleContext);
        ScadaEventDispatcher eventDispatcher = new ScadaEventDispatcher(translator, connector);
        eventDispatcherRegistration = bundleContext.registerService(EventDispatcher.class, eventDispatcher, null);

        LOGGER.info("SCADA dispatcher started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping SCADA dispatcher");

        operationDispatcherRegistration.unregister();
        eventDispatcherRegistration.unregister();
        translator.close();
        getOperation.close();
        setOperation.close();
        connector.close();

        LOGGER.info("SCADA dispatcher stopped");
    }

}
