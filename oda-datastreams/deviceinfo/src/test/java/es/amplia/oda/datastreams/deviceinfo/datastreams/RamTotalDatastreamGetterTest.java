package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RamTotalDatastreamGetterTest.class)
public class RamTotalDatastreamGetterTest {

	@Mock
	DeviceInfoDatastreamsGetter deviceInfoFX30;
	@InjectMocks
	RamTotalDatastreamGetter datastreamGetter;
	@Mock
	NumberFormatException mockedNumberFormatException;

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(DeviceInfoDatastreamsGetter.RAM_TOTAL_DATASTREAM_ID, datastreamGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), datastreamGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		when(deviceInfoFX30.getRamTotal()).thenReturn(2048L);

		assertEquals(2048L, datastreamGetter.get("").get().getValue());
	}

	@Test
	public void testGetException() throws ExecutionException, InterruptedException {
		when(deviceInfoFX30.getRamTotal()).thenThrow(mockedNumberFormatException);

		assertNull(datastreamGetter.get(""));
	}
}
