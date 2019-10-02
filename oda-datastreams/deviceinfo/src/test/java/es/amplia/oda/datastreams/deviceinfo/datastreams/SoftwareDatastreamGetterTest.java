package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SoftwareDatastreamGetterTest.class)
public class SoftwareDatastreamGetterTest {

	@Mock
	DeviceInfoDatastreamsGetter deviceInfoFX30;
	@InjectMocks
	SoftwareDatastreamGetter datastreamGetter;

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(DeviceInfoDatastreamsGetter.SOFTWARE_DATASTREAM_ID, datastreamGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), datastreamGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		when(deviceInfoFX30.getSoftware()).thenReturn(Collections.singletonList(new Software("SO VERSION", "12.34.5", "FIRMWARE")));

		List<Software> list = Collections.singletonList(new Software("SO VERSION", "12.34.5", "FIRMWARE"));

		assertEquals(list.toString(), datastreamGetter.get("").get().getValue());
	}
}
