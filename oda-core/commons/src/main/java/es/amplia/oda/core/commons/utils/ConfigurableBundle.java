package es.amplia.oda.core.commons.utils;

import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;

public interface ConfigurableBundle extends ManagedService, AutoCloseable {
    void persistConfiguration(Dictionary<String, ?> props);
    @Override
    void close();
}
