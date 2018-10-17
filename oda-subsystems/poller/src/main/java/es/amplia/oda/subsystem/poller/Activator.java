package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocatorOsgi;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Activator implements BundleActivator, ManagedService {
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	private Poller poller;
	private ScheduledExecutorService executor;
	private EventDispatcherProxy eventDispatcher;
	
	@Override
	public void start(BundleContext bundleContext) {
		logger.info("Collection subsystem starting");
		DatastreamsGettersLocator datastreamsGettersLocator = new DatastreamsGettersLocatorOsgi(bundleContext);
		eventDispatcher = new EventDispatcherProxy(bundleContext);
		DatastreamsGetterFinder datastreamsGetterFinder = new DatastreamsGetterFinderImpl(datastreamsGettersLocator);
		poller = new PollerImpl(datastreamsGetterFinder, eventDispatcher);
		
	    Dictionary<String, String> props = new Hashtable<>();
	    props.put("service.pid", "es.amplia.opengate.oda.subsystem.collection");
	    bundleContext.registerService(ManagedService.class.getName(), this, props);
	}

	@Override
	public void updated(Dictionary<String, ?> properties) {
		if(properties==null) {
			logger.info("Collection subsystem updated with null properties");
			return;
		}
		
		logger.info("Collection subsystem updated with {} properties", properties.size());
		if(executor!=null) {
			stopPendingOperations();
		}
		executor = new ScheduledThreadPoolExecutor(10);
		
		Map<ConfigurationParser.Key, Set<String>> recolections  = new HashMap<>();
		ConfigurationParser.parse(properties, recolections);
		
		recolections.forEach((key, ids) ->{
			logger.debug("Scheduling recollection of ids={} for deviceIdPattern={} every {} seconds", ids, key.getDeviceIdPattern(), key.getSeconds());
			executor.scheduleWithFixedDelay(() -> poller.runFor(key.getDeviceIdPattern(), ids), key.getSeconds(), key.getSeconds(), TimeUnit.SECONDS);
		});
	}

	private void stopPendingOperations() {
		long timeout = 10;
		
		executor.shutdown();
		try {
			executor.awaitTermination(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("The shutdown of the pool of threads its taking more than {} seconds. Will not wait longer.", timeout);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void stop(BundleContext context) {
		if(executor!=null) {
			stopPendingOperations();
		}
		executor = null;
		poller = null;
		eventDispatcher.close();
		logger.info("Collection subsystem stopped");
	}

}
