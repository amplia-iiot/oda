package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DioZeroI2CService.class)
public class DioZeroI2CServiceTest {
	private final DioZeroI2CService testService = new DioZeroI2CService();

	private int cont1 = 0;
	private int cont2 = 1;
	private int add1 = 104;
	private int add2 = 106;
	private String name1 = "dev1";
	private String name2 = "dev2";
	private String name3 = "dev3";

	@Mock
	I2CDevice dev1;
	@Mock
	I2CDevice dev2;
	@Mock
	I2CDevice dev3;

	@Before
	public void setUp() {
		Map<Integer, Map<Integer, I2CDevice>> devices = new HashMap<>();
		Map<Integer, I2CDevice> devsCont1 = new HashMap<>();
		Map<Integer, I2CDevice> devsCont2 = new HashMap<>();
		devsCont1.put(add1, dev1);
		devsCont1.put(add2, dev2);
		devsCont2.put(add1, dev3);
		devices.put(cont1, devsCont1);
		devices.put(cont2, devsCont2);
		when(dev1.getName()).thenReturn(name1);
		when(dev2.getName()).thenReturn(name2);
		when(dev3.getName()).thenReturn(name3);
		when(dev1.getAddress()).thenReturn(add1);
		when(dev2.getAddress()).thenReturn(add2);
		when(dev3.getAddress()).thenReturn(add1);
		when(dev1.getController()).thenReturn(cont1);
		when(dev2.getController()).thenReturn(cont1);
		when(dev3.getController()).thenReturn(cont2);
		Whitebox.setInternalState(testService, "devices", devices);
	}

	@Test
	public void testGetI2CFromAddress() {
		assertEquals(dev1, testService.getI2CFromAddress(cont1, add1));
		assertEquals(dev2, testService.getI2CFromAddress(cont1, add2));
		assertEquals(dev3, testService.getI2CFromAddress(cont2, add1));
	}

	@Test(expected = I2CDeviceException.class)
	public void testGetI2CFromUnknownAddress() {
		testService.getI2CFromAddress(cont2, add2);
	}

	@Test(expected = I2CDeviceException.class)
	public void testGetI2CFromAddressAndUnknownController() {
		testService.getI2CFromAddress(3, add2);
	}

	@Test
	public void testGetI2CFromName() {
		assertEquals(dev1, testService.getI2CFromName(name1));
		assertEquals(dev2, testService.getI2CFromName(name2));
		assertEquals(dev3, testService.getI2CFromName(name3));
	}

	@Test(expected = I2CDeviceException.class)
	public void testGetI2CFromUnknownName() {
		testService.getI2CFromName("noname");
	}

	@Test
	public void testGetAllI2CFromController() {
		List<I2CDevice> devices1 = testService.getAllI2CFromController(cont1);
		assertTrue(devices1.contains(dev1));
		assertTrue(devices1.contains(dev2));
		List<I2CDevice> devices2 = testService.getAllI2CFromController(cont2);
		assertTrue(devices2.contains(dev3));
	}

	@Test
	public void testGetAllI2CFromUnknownController() {
		List<I2CDevice> devices = testService.getAllI2CFromController(3);

		assertTrue(devices.isEmpty());
	}

	@Test
	public void testGetAllI2C() {
		List<I2CDevice> devices = testService.getAllI2C();

		assertTrue(devices.contains(dev1));
		assertTrue(devices.contains(dev2));
		assertTrue(devices.contains(dev3));
	}

	@Test
	public void testGetAllI2CInANoneDevicesEnvironment() {
		Whitebox.setInternalState(testService, "devices", new HashMap<>());

		List<I2CDevice> devices = testService.getAllI2C();

		assertTrue(devices.isEmpty());
	}

	@Test
	public void testClose() {
		testService.close();

		List<I2CDevice> devices = testService.getAllI2C();

		assertTrue(devices.isEmpty());
	}

	@Test
	public void testLoadConfiguration() {
		List<I2CDevice> devices = Collections.singletonList(dev3);

		testService.loadConfiguration(devices);

		devices = testService.getAllI2C();
		assertFalse(devices.contains(dev1));
		assertFalse(devices.contains(dev2));
		assertTrue(devices.contains(dev3));
	}

	@Test
	public void testLoadAnotherConfiguration() {
		List<I2CDevice> devices = new ArrayList<>();
		devices.add(dev1);
		devices.add(dev2);
		devices.add(dev3);

		testService.loadConfiguration(devices);

		devices = testService.getAllI2C();
		assertTrue(devices.contains(dev1));
		assertTrue(devices.contains(dev2));
		assertTrue(devices.contains(dev3));
	}

	@Test
	public void testLoadEmptyConfiguration() {
		List<I2CDevice> devices = Collections.emptyList();

		testService.loadConfiguration(devices);

		devices = testService.getAllI2C();
		assertFalse(devices.contains(dev1));
		assertFalse(devices.contains(dev2));
		assertFalse(devices.contains(dev3));
	}
}
