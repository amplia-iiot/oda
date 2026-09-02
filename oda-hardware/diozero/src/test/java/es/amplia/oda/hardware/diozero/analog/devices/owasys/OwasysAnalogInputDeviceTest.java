package es.amplia.oda.hardware.diozero.analog.devices.owasys;

import com.diozero.util.RuntimeIOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OwasysAnalogInputDeviceTest {
	@Mock
	private static OwasysAnalogInputDeviceFactory mockedOwasysAnalogInputDevice;
	private final static String KEY = "ADC1";
	private final static int ADC_NUMBER = 1;
	private final static String PATH = "tempfile";
	@Mock
	private RandomAccessFile mockedRandomAccessFile;

	private static OwasysAnalogInputDevice testDevice;

	@BeforeAll
	public static void setUp() throws IOException {
		File file = new File("tempfile");
		FileWriter fw = new FileWriter(file);
		fw.write("3880");
		fw.close();

		testDevice = new OwasysAnalogInputDevice(mockedOwasysAnalogInputDevice, KEY, ADC_NUMBER, PATH);
	}

	@AfterAll
	public static void setDown() {
		File file = new File("tempfile");
		file.delete();
	}

	@Test
	public void constructorExceptionTest() {
		assertThrows(RuntimeIOException.class,
				() -> new OwasysAnalogInputDevice(mockedOwasysAnalogInputDevice, KEY, ADC_NUMBER, "an/unknown/path"));
	}

	//@Test
	public void getValueTest() {
		float value = testDevice.getValue();

		assertEquals(1, value, 0);
	}

	@Test
	public void getValueExceptionTest() throws IOException {
		doThrow(new IOException()).when(mockedRandomAccessFile).seek(anyLong());
		Whitebox.setInternalState(testDevice, "value", mockedRandomAccessFile);

		float value = testDevice.getValue();

		assertEquals(0, value, 0);
	}

	@Test
	public void getAdcNumberTest() {
		int adcNumber = testDevice.getAdcNumber();

		assertEquals(ADC_NUMBER, adcNumber);
	}

	@Test
	public void closeDeviceTest() throws IOException {
		Whitebox.setInternalState(testDevice, "value", mockedRandomAccessFile);

		testDevice.closeDevice();

		verify(mockedRandomAccessFile).close();
	}

	@Test
	public void closeDeviceExceptionTest() throws IOException {
		doThrow(new IOException()).when(mockedRandomAccessFile).close();
		Whitebox.setInternalState(testDevice, "value", mockedRandomAccessFile);

		testDevice.closeDevice();

		verify(mockedRandomAccessFile).close();
	}
}
