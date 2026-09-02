package es.amplia.oda.hardware.diozero.analog.devices.fx30;

import com.diozero.util.RuntimeIOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class Fx30AnalogInputDeviceTest {

	private static final double DELTA = 0.0001;


	@Mock
	private Fx30AnalogInputDeviceFactory mockedFactory;
	private Fx30AnalogInputDevice device;

	@Mock
	private RandomAccessFile mockedRAF;

	@BeforeEach
	public void prepareForTest() throws Exception {
		String path = Files.createTempDirectory("fx30adc").toString() + File.separator;
		File deviceFile = new File(path + 1);
		deviceFile.deleteOnExit();
		try (FileWriter writer = new FileWriter(deviceFile)) {
			writer.write("10000000");
		}

		device = new Fx30AnalogInputDevice(mockedFactory, "testDevice", 1, 1, path, 10f);
		Whitebox.setInternalState(device, "value", mockedRAF);
	}

	@Test
	public void constructorThrowsException() throws Exception {
		assertThrows(RuntimeIOException.class,
				() -> new Fx30AnalogInputDevice(mockedFactory, "testDevice", 1, 1, "an/unknown/path/", 10f));
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

		assertTrue(true, "IO Exception is not caught");
	}
}
