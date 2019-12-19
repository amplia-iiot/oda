package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class GpioDatastreamsSetter implements DatastreamsSetter {

    private static final Logger logger = LoggerFactory.getLogger(GpioDatastreamsSetter.class);

    private final String datastreamId;
    private final int pinIndex;
    private final GpioService gpioService;


    GpioDatastreamsSetter(String datastreamId, int pinIndex, GpioService gpioService) {
        this.datastreamId = datastreamId;
        this.pinIndex = pinIndex;
        this.gpioService = gpioService;
    }

	@Override
	public String getDatastreamIdSatisfied() {
		return datastreamId;
	}

    @Override
    public List<String> getDevicesIdManaged() {
    	return Collections.singletonList("");
    }

	@Override
	public Type getDatastreamType() {
		return Boolean.class;
	}

    @Override
    public CompletableFuture<Void> set(String deviceId, Object value) {
        try {
            Boolean pinValue = (Boolean) value;
            return CompletableFuture.supplyAsync(() -> setDatastreamValue(deviceId, pinIndex, pinValue));
        } catch (ClassCastException e) {
            String errorMessage = createErrorMessage(deviceId, "Data stream value is not valid");
            throw new DataNotFoundException(errorMessage);
        }
    }

    private String createErrorMessage(String deviceId, String description) {
        String errorMessage = String.format("Error setting %s value for %s device: %s", datastreamId, deviceId,
                description);
        logger.warn(errorMessage);
        return errorMessage;
    }

    private Void setDatastreamValue(String deviceId, int pinIndex, boolean value) {
        try {
            GpioPin pin = gpioService.getPinByIndex(pinIndex);

            if (!pin.isOpen()) {
                pin.open();
            }

            pin.setValue(value);
        } catch (GpioDeviceException gpioDeviceException) {
            String errorMessage = createErrorMessage(deviceId, gpioDeviceException.getMessage());
            throw new DataNotFoundException(errorMessage);
        }

        return null;
    }
}
