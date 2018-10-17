package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.osgi.proxies.EventAdminProxy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

import java.io.Closeable;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class ConfigurableBundle implements Closeable {

    private final EventAdminProxy eventAdmin;

    private final ServiceRegistration<ManagedService> configServiceRegistration;


    public ConfigurableBundle(BundleContext bundleContext, ConfigurationUpdateHandler handler,
                              List<ServiceRegistration<?>> serviceInterfacesToNotify) {
        String bundleName = bundleContext.getBundle().getSymbolicName();
        this.eventAdmin = new EventAdminProxy(bundleContext);
        Dictionary<String, Object> managedServiceProps = new Hashtable<>();
        managedServiceProps.put(Constants.SERVICE_PID, bundleName);
        ConfigurableBundleNotifierService configBundleService =
                new ConfigurableBundleNotifierService(bundleName, handler, eventAdmin, serviceInterfacesToNotify);
        this.configServiceRegistration =
                bundleContext.registerService(ManagedService.class, configBundleService, managedServiceProps);
    }

    public ConfigurableBundle(BundleContext bundleContext, ConfigurationUpdateHandler handler) {
        this(bundleContext, handler, Collections.emptyList());
    }

    public void persistConfiguration(Dictionary<String, ?> props) {
        configServiceRegistration.setProperties(props);
    }

    @Override
    public void close() {
        configServiceRegistration.unregister();
        eventAdmin.close();
    }
}
