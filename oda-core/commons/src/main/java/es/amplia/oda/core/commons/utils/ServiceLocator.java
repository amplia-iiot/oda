package es.amplia.oda.core.commons.utils;

import java.util.List;

/**
 * Interface to locate services available in the system
 * @param <T> Type of the service to locate
 */
public interface ServiceLocator<T> extends AutoCloseable {
    /**
     * Find all the services of type T available in the system.
     * @return The list of T services available in the system.
     */
    List<T> findAll();

    @Override
    void close();
}
