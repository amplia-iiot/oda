package es.amplia.oda.hardware.diozero.analog.devices.owasys;

import com.diozero.util.RuntimeIOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OwasysAnalogInputDevice.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class OwasysAnalogInputDeviceTest {
	@Mock
	private static OwasysAnalogInputDeviceFactory mockedOwasysAnalogInputDevice;
	private final static String KEY = "ADC1";
	private final static int ADC_NUMBER = 1;
	private final static String PATH = "tempfile";
	@Mock
	private RandomAccessFile mockedRandomAccessFile;

	private static OwasysAnalogInputDevice testDevice;

	@BeforeClass
	public static void setUp() throws IOException {
		File file = new File("tempfile");
		FileWriter fw = new FileWriter(file);
		fw.write("3880");
		fw.close();

		testDevice = new OwasysAnalogInputDevice(mockedOwasysAnalogInputDevice, KEY, ADC_NUMBER, PATH);
	}

	@AfterClass
	public static void setDown() {
		File file = new File("tempfile");
		file.delete();
	}

	@Test (expected = RuntimeIOException.class)
	public void constructorExceptionTest() {
		testDevice = new OwasysAnalogInputDevice(mockedOwasysAnalogInputDevice, KEY, ADC_NUMBER, "an/unknown/path");
	}

	//@Test
	public void getValueTest() {
		float value = testDevice.getValue();

		assertEquals(1, value, 0);
	}

	@Test
	public void getValueExceptionTest() throws IOException {
		doThrow(new IOException()).when(mockedRandomAccessFile).seek(anyInt());
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
