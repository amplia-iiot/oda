package es.amplia.oda.hardware.diozero.analog.devices.owasys;

import com.diozero.api.DeviceMode;
import com.diozero.api.PinInfo;
import com.diozero.util.BoardPinInfo;
import es.amplia.oda.core.commons.adc.DeviceType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OwasysAnalogInputDeviceFactory.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class OwasysAnalogInputDeviceFactoryTest {
	private static final String OWA_ANA_INP_NAME_1 = "ADC1";
	private static final String OWA_ANA_INP_NAME_2 = "ADC2";
	private static final boolean ACTIVE_LOW_1 = false;
	private static final boolean ACTIVE_LOW_2 = true;
	private static final String PATH_TO_ANA_INP_1 = "path/to/analog/input-1";
	private static final String PATH_TO_ANA_INP_2 = "path/to/analog/input-2";
	private static final DeviceType DEVICE_TYPE_1 = DeviceType.OWASYS;
	private static final DeviceType DEVICE_TYPE_2 = DeviceType.OWASYS;
	private static final PinInfo PIN_INFO_1 = new PinInfo("test", "head", 1, 1, "AIN1", Collections.singleton(DeviceMode.ANALOG_INPUT));
	private static final PinInfo PIN_INFO_2 = new PinInfo("test", "head", 2, 2, "AIN2", Collections.singleton(DeviceMode.ANALOG_INPUT));

	private static OwasysAnalogInputDeviceFactory factoryOwa1;
	private static OwasysAnalogInputDeviceFactory factoryOwa2;

	private static boolean createdFile;

	@BeforeClass
	public static void setUp() throws IOException {
		File file1 = new File(PATH_TO_ANA_INP_1);
		File file2 = new File(PATH_TO_ANA_INP_2);
		createdFile = file1.getParentFile().mkdirs();
		FileWriter writer1 = new FileWriter(file1);
		FileWriter writer2 = new FileWriter(file2);
		writer1.write("1");
		writer2.write("2");
		writer1.close();
		writer2.close();

		factoryOwa1 = new OwasysAnalogInputDeviceFactory(OWA_ANA_INP_NAME_1, PATH_TO_ANA_INP_1, ACTIVE_LOW_1, DEVICE_TYPE_1);
		factoryOwa2 = new OwasysAnalogInputDeviceFactory(OWA_ANA_INP_NAME_2, PATH_TO_ANA_INP_2, ACTIVE_LOW_2, DEVICE_TYPE_2);
	}

	@AfterClass
	public static void setDown() {
		boolean cont = true;
		File file1 = new File(PATH_TO_ANA_INP_1);
		if(file1.exists()) {
			file1.delete();
		}
		if(createdFile) {
			String temp = PATH_TO_ANA_INP_2;
			do {
				File file2 = new File(temp);
				if(file2.exists()) {
					file2.delete();
				}
				if(temp.split("/").length > 1) {
					temp = temp.substring(0, temp.lastIndexOf("/"));
				} else {
					cont = false;
				}
			} while(cont);
		}
	}

	@Test
	public void testCreateAnalogInputDeviceTest() {
		OwasysAnalogInputDevice inputDevice1 = factoryOwa1.createAnalogInputDevice("key1", PIN_INFO_1);
		OwasysAnalogInputDevice inputDevice2 = factoryOwa2.createAnalogInputDevice("key2", PIN_INFO_2);

		assertEquals(1./3880f, inputDevice1.getValue(), 1/3880f);
		assertEquals(2./3880f, inputDevice2.getValue(), 1/3880f);
	}

	@Test
	public void testGetVRefTest() {
		float vRef1 = factoryOwa1.getVRef();
		float vRef2 = factoryOwa2.getVRef();

		assertEquals(10f, vRef1, 0);
		assertEquals(5f, vRef2, 0);
	}

	@Test
	public void testGetName() {
		String name1 = factoryOwa1.getName();
		String name2 = factoryOwa2.getName();

		assertEquals(OWA_ANA_INP_NAME_1, name1);
		assertEquals(OWA_ANA_INP_NAME_2, name2);
	}

	@Test
	public void testGetBoardPinInfo() {
		BoardPinInfo boardInfo1 = factoryOwa1.getBoardPinInfo();
		BoardPinInfo boardInfo2 = factoryOwa2.getBoardPinInfo();

		assertNotNull(boardInfo1);
		assertNotNull(boardInfo2);
	}
}
