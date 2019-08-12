package es.amplia.oda.core.commons.osgi.proxies;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;

public class ConfigurationAdminProxy implements ConfigurationAdmin, AutoCloseable {

    private final OsgiServiceProxy<ConfigurationAdmin> proxy;


    public ConfigurationAdminProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(ConfigurationAdmin.class, bundleContext);
    }

    @Override
    public Configuration createFactoryConfiguration(String factoryPid) {
        return proxy.callFirst(configurationAdmin -> {
            try {
                return configurationAdmin.createFactoryConfiguration(factoryPid);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Configuration createFactoryConfiguration(String factoryPid, String location) {
        return proxy.callFirst(configurationAdmin -> {
            try {
                return configurationAdmin.createFactoryConfiguration(factoryPid, location);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Configuration getConfiguration(String pid) {
        return proxy.callFirst(configurationAdmin -> {
            try {
                return configurationAdmin.getConfiguration(pid);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Configuration getConfiguration(String pid, String location) {
        return proxy.callFirst(configurationAdmin -> {
            try {
                return configurationAdmin.getConfiguration(pid, location);
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public Configuration[] listConfigurations(String filter) {
        return proxy.callFirst(configurationAdmin -> {
            try {
                return configurationAdmin.listConfigurations(filter);
            } catch(IOException| InvalidSyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void close() {
        proxy.close();
    }
}
