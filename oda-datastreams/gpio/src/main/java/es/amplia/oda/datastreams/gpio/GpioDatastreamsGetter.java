package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class GpioDatastreamsGetter implements DatastreamsGetter {

    private static final Logger logger = LoggerFactory.getLogger(GpioDatastreamsGetter.class);

    private final String datastreamId;
    private final int pinIndex;
    private final GpioService gpioService;


    GpioDatastreamsGetter(String datastreamId, int pinIndex, GpioService gpioService) {
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
	public CompletableFuture<CollectedValue> get(String device) {
        return CompletableFuture.supplyAsync(() -> getDatastreamIdValuesForDevicePattern(device));
    }

    private CollectedValue getDatastreamIdValuesForDevicePattern(String device) {
        try {
            GpioPin pin = gpioService.getPinByIndex(pinIndex);

            if (!pin.isOpen()) {
                pin.open();
            }

	        long at = System.currentTimeMillis();
			Boolean value = pin.getValue();
			return new CollectedValue(at , value );
        } catch (GpioDeviceException gpioDeviceException) {
            String msg = String.format("Error getting %s value for %s device: %s", datastreamId, device,
                    gpioDeviceException.getMessage());
			logger.warn(msg);
            throw new DataNotFoundException(msg);
        }
    }
}
