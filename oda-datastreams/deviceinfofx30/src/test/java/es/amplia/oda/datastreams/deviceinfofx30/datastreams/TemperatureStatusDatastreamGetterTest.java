package es.amplia.oda.datastreams.deviceinfofx30.datastreams;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;
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
@PrepareForTest(TemperatureStatusDatastreamGetterTest.class)
public class TemperatureStatusDatastreamGetterTest {

	@Mock
	DeviceInfoFX30 deviceInfoFX30;
	@InjectMocks
	TemperatureStatusDatastreamGetter datastreamGetter;

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(DeviceInfoFX30.TEMPERATURE_STATUS_DATASTREAM_ID, datastreamGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), datastreamGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		when(deviceInfoFX30.getTemperatureStatus()).thenReturn("OK");

		assertEquals("OK", datastreamGetter.get("").get().getValue());
	}
}
