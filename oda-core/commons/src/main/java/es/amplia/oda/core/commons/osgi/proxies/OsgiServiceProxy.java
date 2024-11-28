package es.amplia.oda.core.commons.osgi.proxies;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OSGi service proxy to make the usage of OSGi services through the service registry easier.
 * @param <S> OSGi service type to make the proxy.
 */
public class OsgiServiceProxy<S> implements AutoCloseable {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OsgiServiceProxy.class);
    private static final String NO_SERVICE_IMPLEMENTATION_MESSAGE = "No service implementation of {} available in OSGi";

    /**
     * Class type parameter of the proxy.
     */
    private final Class<S> typeClassParameter;

    /**
     * OSGi service tracker.
     */
    private final ServiceTracker<S, S> serviceTracker;

    /**
     * Constructor.
     * @param typeParameterClass Class type parameter of the proxy.
     * @param bundleContext OSGi bundle context.
     */
    public OsgiServiceProxy(Class<S> typeParameterClass, BundleContext bundleContext) {
        typeClassParameter = typeParameterClass;
        serviceTracker = new ServiceTracker<>(bundleContext, typeParameterClass, null);
        serviceTracker.open();
    }

    /**
     * Constructor.
     * @param typeParameterClass Class type parameter of the proxy.
     * @param filterProperties Properties to filter the proxy.
     * @param bundleContext OSGi bundle context.
     */
    public OsgiServiceProxy(Class<S> typeParameterClass, Map<String, String> filterProperties, BundleContext bundleContext) {
        ServiceTracker<S, S> tracker;
        this.typeClassParameter = typeParameterClass;
        try {
            Filter filter = bundleContext.createFilter(createFilter(typeClassParameter, filterProperties));
            tracker = new ServiceTracker<>(bundleContext, filter, null);
        } catch (InvalidSyntaxException e) {
            LOGGER.error("Filter is not valid. Creating Service Tracker from class parameter type", e);
            tracker = new ServiceTracker<>(bundleContext, typeClassParameter, null);
        }
        serviceTracker = tracker;
        serviceTracker.open();
    }

    private String createFilter(Class<S> clazz, Map<String, String> filterProperties) {
        String propertiesToFilter =
                filterProperties.entrySet().stream()
                        .map(entry -> " (" + entry.getKey() + "=" + entry.getValue() + ")")
                        .reduce(String::concat)
                        .orElse("");

        return "(&(" + Constants.OBJECTCLASS + "=" + clazz.getName() + ")" + propertiesToFilter + ")";
    }

    /**
     * Call the service method through the proxy.
     * The first service available will be called if it is any available.
     * @param serviceMethod Service method to call.
     * @param <T> Return type of the method.
     * @return The method result or null if no service is available.
     */
    public <T> T callFirst(Function<S, T> serviceMethod) {
        S real = serviceTracker.getService();
        if(real==null) {
            LOGGER.warn(NO_SERVICE_IMPLEMENTATION_MESSAGE, typeClassParameter.getName());
            return null;
        } else {
            LOGGER.trace("Passing request to real implementation");
            return serviceMethod.apply(real);
        }
    }

    /**
     * Consume the service method through the proxy.
     * The first service available will be consume if it is any available.
     * @param serviceMethod Service method to consume.
     */
    public void consumeFirst(Consumer<S> serviceMethod) {
        S real = serviceTracker.getService();
        if(real==null) {
            LOGGER.warn(NO_SERVICE_IMPLEMENTATION_MESSAGE, typeClassParameter.getName());
        } else {
            LOGGER.trace("Passing request to real implementation");
            serviceMethod.accept(real);
        }
    }

    /**
     * Call the service method through the proxy in all registered services available.
     * @param serviceMethod Service method to call.
     * @param <T> Return type of the method.
     * @return List of results of all registered services available.
     */
    public <T> List<T> callAll(Function<S, T> serviceMethod) {
        Object[] reals = serviceTracker.getServices();
        if(reals==null) {
            LOGGER.warn(NO_SERVICE_IMPLEMENTATION_MESSAGE, typeClassParameter.getName());
            return Collections.emptyList();
        } else {
            LOGGER.trace("Passing request to real implementations");
            return Arrays.stream(reals).filter(typeClassParameter::isInstance)
                    .map(typeClassParameter::cast)
                    .map(serviceMethod)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Consume the service method through the proxy in all registered services available.
     * @param serviceMethod Service method to consume.
     */
    public void consumeAll(Consumer<S> serviceMethod) {
        Object[] reals = serviceTracker.getServices();
        if(reals==null) {
            LOGGER.warn(NO_SERVICE_IMPLEMENTATION_MESSAGE, typeClassParameter.getName());
        } else {
            LOGGER.trace("Passing request to real implementations");
            Arrays.stream(reals).filter(typeClassParameter::isInstance)
                    .map(typeClassParameter::cast)
                    .forEach(serviceMethod);
        }
    }

    @Override
    public void close() {
        serviceTracker.close();
    }
}
