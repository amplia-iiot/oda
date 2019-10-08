package es.amplia.oda.datastreams.diozero.datastreams.adc;

import es.amplia.oda.core.commons.diozero.AdcChannel;
import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.diozero.datastreams.AbstractDatastreamsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class AdcDatastreamsGetter extends AbstractDatastreamsGetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatastreamsGetter.class);

	private final int pinIndex;
	private final AdcService adcService;

	public AdcDatastreamsGetter(String datastreamId, int pinIndex, AdcService adcService, Executor executor) {
		super(datastreamId, executor);
		this.pinIndex = pinIndex;
		this.adcService = adcService;
	}

	protected DatastreamsGetter.CollectedValue getDatastreamIdValuesForDevicePattern(String device) {
		try {
			AdcChannel channel = adcService.getChannelByIndex(pinIndex);

			long at = System.currentTimeMillis();
			Float value = channel.getScaledValue();
			return new DatastreamsGetter.CollectedValue(at , value );
		} catch (GpioDeviceException gpioDeviceException) {
			String msg = String.format("Error getting %s value for %s device: %s", getDatastreamId(), device,
					gpioDeviceException.getMessage());
			LOGGER.warn(msg);
			throw new DataNotFoundException(msg);
		}
	}
}
