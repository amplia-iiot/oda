package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocatorOsgi;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration<?> registration;

	@Override
	public void start(BundleContext context) {
		logger.info("Starting Operation Get Activator");
		DatastreamsGettersLocator datastreamsGettersLocator = new DatastreamsGettersLocatorOsgi(context);
		DatastreamsGettersFinderImpl datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
		OperationGetDeviceParameters getter = new OperationGetDeviceParametersImpl(datastreamsGettersFinder);

		registration = context.registerService(OperationGetDeviceParameters.class.getName(), getter, null);
		logger.info("Operation Get Activator started");
	}

	@Override
	public void stop(BundleContext context) {
		logger.info("Stopping Operation Get Activator");
		registration.unregister();
		logger.info("Operation Get Activator stopped");
	}
}
