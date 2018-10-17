package es.amplia.oda.core.commons.interfaces;

import java.util.Optional;

public interface Interceptor<T> {
    Optional<T> intercept(Optional<T> param);
}
