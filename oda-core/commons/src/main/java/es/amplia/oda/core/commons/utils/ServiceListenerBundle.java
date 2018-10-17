package es.amplia.oda.core.commons.utils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceListenerBundle<S> implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ServiceListenerBundle.class);

    private final BundleContext bundleContext;
    private final ServiceListener serviceListener;

    public ServiceListenerBundle(BundleContext bundleContext, Class<S> serviceToListen, Runnable serviceChangedAction) {
        this.bundleContext = bundleContext;
        this.serviceListener = event -> serviceChangedAction.run();
        try {
            bundleContext.addServiceListener(serviceListener,
                    String.format("(%s=%s)", Constants.OBJECTCLASS, serviceToListen.getName()));
        } catch (InvalidSyntaxException e) {
            logger.warn("Can not add service listener of {}", serviceToListen.getName());
        }
    }

    @Override
    public void close() {
        bundleContext.removeServiceListener(serviceListener);
    }
}
