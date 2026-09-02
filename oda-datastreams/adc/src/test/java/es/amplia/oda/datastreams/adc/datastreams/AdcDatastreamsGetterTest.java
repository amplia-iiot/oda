package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

	@BeforeEach
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

	@Test
	public void testGetThrowsExecutionException() throws ExecutionException, InterruptedException {
		when(mockedService.getChannelByIndex(TEST_INDEX)).thenThrow(new AdcDeviceException(""));

		assertThrows(ExecutionException.class, () -> testGetter.get("").get());
	}
}
