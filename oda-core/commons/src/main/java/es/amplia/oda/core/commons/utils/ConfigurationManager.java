package es.amplia.oda.core.commons.utils;

import java.io.IOException;
import java.util.Optional;

public interface ConfigurationManager {
    Optional<String> getConfiguration(String pid, String property) throws IOException;
    void updateConfiguration(String pid, String property, Object value) throws IOException;
}
