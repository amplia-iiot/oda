package es.amplia.oda.datastreams.diozero.datastreams.adc;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AdcDatastreamsGetterTest {
	private final String datastreamId = "temperature";
	private final int pinIndex = 0;
	@Mock
	AdcService mockedService;
	@Mock
	Executor mockedExecutor;
	private AdcDatastreamsGetter testGetter;
	@Mock
	AdcChannel mockedChannel;

	@Before
	public void prepareForTest() {
		testGetter = new AdcDatastreamsGetter(datastreamId, pinIndex, mockedService, mockedExecutor);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		String datastreamId = testGetter.getDatastreamIdSatisfied();

		assertEquals(this.datastreamId, datastreamId);
	}

	@Test
	public void testGetDevicesIdManaged() {
		List<String> datastreamIds = testGetter.getDevicesIdManaged();

		assertEquals(Collections.singletonList(""), datastreamIds);
	}

	@Test
	public void testGetDatastreamIdValuesForDevicePattern() {
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);
		when(mockedChannel.getScaledValue()).thenReturn(3.14f);

		testGetter.getDatastreamIdValuesForDevicePattern(datastreamId);
	}

	@Test(expected = DataNotFoundException.class)
	public void testGetDatastreamIdValuesForDevicePatternWithException() {
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);
		when(mockedChannel.getScaledValue()).thenThrow(new GpioDeviceException(""));

		testGetter.getDatastreamIdValuesForDevicePattern(datastreamId);
	}



	@Test
	public void testGet() {
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);
		when(mockedChannel.getScaledValue()).thenReturn(3.14f);

		testGetter.get(datastreamId);
	}
}
