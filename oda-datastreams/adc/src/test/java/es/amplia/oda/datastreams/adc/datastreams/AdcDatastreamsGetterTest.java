package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AdcDatastreamsGetterTest {

	private static final String TEST_DATASTREAM = "testDatastream";
	private static final int TEST_INDEX = 1;
	private static final float TEST_VALUE = 3.14f;
	private static final double TEST_MINIMUM = 0;
	private static final double TEST_MAXIMUM = 1;


	@Mock
	AdcService mockedService;
	private AdcDatastreamsGetter testGetter;

	@Mock
	AdcChannel mockedChannel;

	@Before
	public void prepareForTest() {
		testGetter = new AdcDatastreamsGetter(TEST_DATASTREAM, TEST_INDEX, mockedService, TEST_MINIMUM, TEST_MAXIMUM);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(TEST_DATASTREAM, testGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), testGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		when(mockedService.getChannelByIndex(TEST_INDEX)).thenReturn(mockedChannel);
		when(mockedChannel.getScaledValue()).thenReturn(TEST_VALUE);

		CompletableFuture<DatastreamsGetter.CollectedValue> future = testGetter.get("");
		DatastreamsGetter.CollectedValue collectedValue = future.get();

		assertEquals(TEST_VALUE, collectedValue.getValue());
		assertNotEquals(0L, collectedValue.getAt());
		assertEquals(Collections.emptyList(), collectedValue.getPath());
	}

	@Test(expected = ExecutionException.class)
	public void testGetThrowsExecutionException() throws ExecutionException, InterruptedException {
		when(mockedService.getChannelByIndex(TEST_INDEX)).thenThrow(new AdcDeviceException(""));

		testGetter.get("").get();

		fail("Execution Exception should be thrown");
	}
}
