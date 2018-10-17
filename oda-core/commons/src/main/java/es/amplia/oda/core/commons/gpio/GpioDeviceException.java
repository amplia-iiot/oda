package es.amplia.oda.core.commons.gpio;

/**
 * Exception thrown on GPIO device operations.
 */
public class GpioDeviceException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5153107718227453813L;

	/**
     * Constructor.
     * @param message Message explaining the GPIO device exception.
     */
    public GpioDeviceException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message Message explaining the GPIO device exception.
     * @param cause Cause of the GPIO device exception.
     */
    public GpioDeviceException(String message, Throwable cause) {
        super(message, cause);
    }

}
