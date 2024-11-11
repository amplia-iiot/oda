package es.amplia.oda.hardware.opcua;

import es.amplia.oda.core.commons.opcua.OpcUaConnection;
import es.amplia.oda.core.commons.osgi.proxies.ScadaTableTranslatorProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.hardware.opcua.configuration.OpcUaClientConfigurationUpdateHandler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<OpcUaConnection> opcuaServiceRegistration;
    private ConfigurableBundle configurableBundle;
    private OpcUaClientConfigurationUpdateHandler configHandler;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting OPC-UA Request bundle");
        es.amplia.oda.hardware.opcua.internal.OpcUaConnection client = new es.amplia.oda.hardware.opcua.internal.OpcUaConnection();
        opcuaServiceRegistration =
                bundleContext.registerService(OpcUaConnection.class, client, null);
        configHandler = new OpcUaClientConfigurationUpdateHandler(new EventDispatcherProxy(bundleContext), new ScadaTableTranslatorProxy(bundleContext), client);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        LOGGER.info("OPC-UA Request bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping OPC-UA Request bundle");
        configHandler.close();
        configurableBundle.close();
        opcuaServiceRegistration.unregister();
        LOGGER.info("OPC-UA Request bundle stopped");
    }
}
