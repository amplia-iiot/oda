package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.configuration.Iec104ConnectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.utils.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private Iec104Connector connector;

	private ScadaDispatcherProxy dispatcher;
	private Iec104ConnectorConfigurationUpdateHandler configHandler;
	private ConfigurableBundle configurableBundle;
	private ServiceListenerBundle<ScadaTableInfo> serviceListenerBundle;

	private ServiceRegistration<ScadaConnector> scadaConnectorServiceRegistration;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting IEC 104 connector");

		dispatcher = new ScadaDispatcherProxy(bundleContext);
		connector = new Iec104Connector(dispatcher);
		scadaConnectorServiceRegistration =
				bundleContext.registerService(ScadaConnector.class, connector, null);
		configHandler = new Iec104ConnectorConfigurationUpdateHandler(connector);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);

		serviceListenerBundle = new ServiceListenerBundle<>(bundleContext, ScadaTableInfo.class, this::onServiceChange);

		LOGGER.info("IEC 104 connector started");
	}

	void onServiceChange() {
		configHandler.applyConfiguration();
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping IEC 104 connector");

		scadaConnectorServiceRegistration.unregister();
		serviceListenerBundle.close();
		configurableBundle.close();
		connector.close();
		dispatcher.close();

		LOGGER.info("IEC 104 connector stopped");
	}
}

