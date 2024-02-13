package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class DioZeroAdcEventTest {

	private static final long epochTime = System.currentTimeMillis();
	private static final long nanoTime = System.nanoTime();
	private static final float unscaledValue = 0.3f;
	private static final int pinNumber = 5;
	private static final float range = 10f;

	private DioZeroAdcEvent adcEvent;

	@Before
	public void prepareForTest() {
		AnalogInputEvent event = new AnalogInputEvent(pinNumber, epochTime, nanoTime, unscaledValue);
		event.setRange(range);
		adcEvent = new DioZeroAdcEvent(event);
	}

	@Test
	public void testGetGpio() {
		assertEquals(pinNumber, adcEvent.getGpio());
	}

	@Test
	public void testGetEpochTime() {
		assertEquals(Float.valueOf(epochTime), Float.valueOf(adcEvent.getEpochTime()));
	}

	@Test
	public void testGetRange() {
		assertEquals(Float.valueOf(range), Float.valueOf(adcEvent.getRange()));
	}

	@Test
	public void testSetRange() {
		float newValue = 5f;

		adcEvent.setRange(newValue);

		AnalogInputEvent event = (AnalogInputEvent) Whitebox.getInternalState(adcEvent, "event");
		assertEquals(Float.valueOf(event.getRange()), Float.valueOf(newValue));
	}

	@Test
	public void testGetScaledValue() {
		assertEquals(Float.valueOf(range * unscaledValue), Float.valueOf(adcEvent.getScaledValue()) );
	}

	@Test
	public void testGetUnscaledValue() {
		assertEquals(Float.valueOf(unscaledValue), Float.valueOf(adcEvent.getUnscaledValue()) );
	}
}
