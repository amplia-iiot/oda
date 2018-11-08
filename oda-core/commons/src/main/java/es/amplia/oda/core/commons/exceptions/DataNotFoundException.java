package es.amplia.oda.core.commons.exceptions;

/**
 * Data not found exception for finding methods in ODA services.
 */
public class DataNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 2074220411499422789L;

    /**
     * Constructor.
     * @param message Exception message.
     */
    public DataNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message Exception message.
     * @param throwable Inner exception.
     */
    public DataNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
