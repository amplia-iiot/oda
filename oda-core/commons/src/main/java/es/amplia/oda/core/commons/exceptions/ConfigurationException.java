package es.amplia.oda.core.commons.exceptions;

/**
 * Exception during the configuration of a bundle.
 */
@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException {

    /**
     * Constructor.
     * @param message Message of the exception.
     */
    public ConfigurationException(String message) {
        super(message);
    }
}
