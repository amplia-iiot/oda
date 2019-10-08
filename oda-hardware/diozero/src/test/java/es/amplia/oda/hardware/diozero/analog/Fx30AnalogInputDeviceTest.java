package es.amplia.oda.hardware.diozero.analog;

import com.diozero.util.RuntimeIOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Fx30AnalogInputDevice.class)
public class Fx30AnalogInputDeviceTest {

	private Fx30AnalogInputDevice device;
	@Mock
	Fx30AnalogInputDeviceFactory mockedFactory;
	@Mock
	RandomAccessFile mockedRAF;

	@Before
	public void prepareForTest() throws Exception {
		when(mockedFactory.getVRef()).thenReturn(10f);
		whenNew(RandomAccessFile.class).withAnyArguments().thenReturn(mockedRAF);
		device = new Fx30AnalogInputDevice(mockedFactory, "testDevice", 1, 1, "", 10f);
	}

	@Test
	public void closeDeviceTest() throws IOException {

		device.closeDevice();

		verify(mockedRAF).close();
	}

	@Test
	public void closeDeviceTestExceptionCaught() throws IOException {
		doThrow(new IOException()).when(mockedRAF).close();

		device.closeDevice();
	}

	@Test
	public void getValueTest() throws IOException {
		when(mockedRAF.readLine()).thenReturn("10000000");

		assertEquals(Float.valueOf(10000000f / 10f / 1000000f), Float.valueOf(device.getValue()));
	}

	@Test(expected = RuntimeIOException.class)
	public void getValueTestWithException() throws IOException {
		when(mockedRAF.readLine()).thenThrow(new IOException());

		device.getValue();
	}

	@Test
	public void testGetAdcNumber() {
		assertEquals(1, device.getAdcNumber());
	}

	@Test
	public void testGetvRef() {
		assertEquals(Float.valueOf(10f), Float.valueOf(device.getvRef()));
	}

	@Test
	public void testGetPinNumber() {
		assertEquals(1, device.getPinNumber());
	}

	@Test
	public void testGetName() {
		assertEquals("testDevice", device.getName());
	}

	@Test
	public void testIsActiveLow() {
		assertFalse(device.isActiveLow());
	}

	@Test
	public void testGetPath() {
		assertEquals("", device.getPath());
	}
}
