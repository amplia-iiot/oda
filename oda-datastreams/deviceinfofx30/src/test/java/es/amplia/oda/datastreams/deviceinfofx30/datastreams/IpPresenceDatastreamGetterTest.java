package es.amplia.oda.datastreams.deviceinfofx30.datastreams;

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
@PrepareForTest(IpPresenceDatastreamGetterTest.class)
public class IpPresenceDatastreamGetterTest {

	@Mock
	DeviceInfoFX30 deviceInfoFX30;
	@InjectMocks
	IpPresenceDatastreamGetter datastreamGetter;

	@Test
	public void testGetDatastreamIdSatisfied() {
		assertEquals(DeviceInfoFX30.IP_PRESENCE_DATASTREAM_ID, datastreamGetter.getDatastreamIdSatisfied());
	}

	@Test
	public void testGetDevicesIdManaged() {
		assertEquals(Collections.singletonList(""), datastreamGetter.getDevicesIdManaged());
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		when(deviceInfoFX30.getIpPresence()).thenReturn("Yes");

		assertEquals("Yes", datastreamGetter.get("").get().getValue());
	}
}
