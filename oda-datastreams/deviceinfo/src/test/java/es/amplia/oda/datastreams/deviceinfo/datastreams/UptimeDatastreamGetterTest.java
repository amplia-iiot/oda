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
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UptimeDatastreamGetter.class)
public class UptimeDatastreamGetterTest {

	@Mock
	DeviceInfoDatastreamsGetter deviceInfoFX30;
	@InjectMocks
	UptimeDatastreamGetter datastreamGetter;

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(DeviceInfoDatastreamsGetter.UPTIME_DATASTREAM_ID, datastreamGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), datastreamGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		when(deviceInfoFX30.getUptime()).thenReturn(3600L);

		assertEquals(3600L, datastreamGetter.get("").get().getValue());
	}
}
