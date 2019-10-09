package es.amplia.oda.hardware.diozero.analog;

import com.diozero.util.RuntimeIOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Fx30AnalogInputDevice.class, RandomAccessFile.class})
public class Fx30AnalogInputDeviceTest {

	private static final double DELTA = 0.0001;


	@Mock
	private Fx30AnalogInputDeviceFactory mockedFactory;
	private Fx30AnalogInputDevice device;

	@Mock
	private RandomAccessFile mockedRAF;

	@Before
	public void prepareForTest() throws Exception {
		when(mockedFactory.getVRef()).thenReturn(10f);
		whenNew(RandomAccessFile.class).withAnyArguments().thenReturn(mockedRAF);
		device = new Fx30AnalogInputDevice(mockedFactory, "testDevice", 1, 1, "", 10f);
	}

	@Test(expected = RuntimeIOException.class)
	public void constructorThrowsException() throws Exception {
		whenNew(RandomAccessFile.class).withParameterTypes(File.class, String.class)
				.withArguments(any(File.class), anyString()).thenThrow(new FileNotFoundException());

		new Fx30AnalogInputDevice(mockedFactory, "testDevice", 1, 1, "", 10f);

		fail("Runtime IO Exception should be thrown");
	}

	@Test
	public void getValueTest() throws IOException {
		when(mockedRAF.readLine()).thenReturn("10000000");

		assertEquals(Float.valueOf(10000000f / 10f / 1000000f), Float.valueOf(device.getValue()));
	}

	@Test
	public void getValueTestWithException() throws IOException {
		when(mockedRAF.readLine()).thenThrow(new IOException());

		assertEquals(0, device.getValue(), DELTA);
	}

	@Test
	public void testGetAdcNumber() {
		assertEquals(1, device.getAdcNumber());
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

		assertTrue("IO Exception is not caught", true);
	}
}
