package es.amplia.oda.hardware.diozero.analog;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class DioZeroAdcServiceTest {

	private DioZeroAdcService testService;

	@Mock
	AdcChannel ADC0;
	@Mock
	AdcChannel ADC1;
	@Mock
	AdcChannel ADC2;

	@Before
	public void prepareForTest() {
		when(ADC0.getName()).thenReturn("I'm not");
		when(ADC1.getName()).thenReturn("I am");

		Map<Integer, AdcChannel> channels = new HashMap<>();
		channels.put(0, ADC0);
		channels.put(1, ADC1);

		testService = new DioZeroAdcService();

		Whitebox.setInternalState(testService, "channels", channels);
	}

	@Test
	public void testGetChannelByName() {
		assertEquals(ADC1, testService.getChannelByName(ADC1.getName()));
	}

	@Test(expected = AdcDeviceException.class)
	public void testGetChannelByNameWithException() {
		testService.getChannelByName("No name");
	}

	@Test
	public void testGetChannelByIndex() {
		assertEquals(ADC0, testService.getChannelByIndex(0));
	}

	@Test(expected = AdcDeviceException.class)
	public void testGetChannelByIndexWithException() {
		testService.getChannelByIndex(-1);
	}

	@Test
	public void testGetAvailableChannels() {
		Map<Integer, AdcChannel> channels = testService.getAvailableChannels();

		assertEquals(2, channels.size());
		assertEquals(ADC0, channels.get(0));
		assertEquals(ADC1, channels.get(1));
		assertNull(channels.get(2));
	}

	@Test
	public void testAddConfiguredPin() {
		Map<Integer, AdcChannel> channels = new HashMap<>();
		AdcChannel oldChannel = mock(AdcChannel.class);
		channels.put(9, oldChannel);
		Whitebox.setInternalState(testService, "channels", channels);

		when(ADC1.getIndex()).thenReturn(0);
		when(ADC1.getIndex()).thenReturn(1);
		when(ADC2.getIndex()).thenReturn(2);

		testService.loadConfiguration(Arrays.asList(ADC0, ADC1, ADC2));

		verify(oldChannel).close();
		assertEquals(3, channels.size());
		assertEquals(ADC0, channels.get(0));
		assertEquals(ADC1, channels.get(1));
		assertEquals(ADC2, channels.get(2));
		assertNull(channels.get(9));
	}

	@Test
	public void testRelease() {
		testService.close();

		verify(ADC0).close();
		verify(ADC1).close();
	}

	@Test
	public void testReleaseWithException() {
		doThrow(AdcDeviceException.class).when(ADC1).close();

		testService.close();

		verify(ADC0).close();
	}
}
