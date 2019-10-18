package es.amplia.oda.hardware.i2c;

import com.diozero.api.I2CDevice;
import es.amplia.oda.hardware.i2c.configuration.DioZeroI2CConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DioZeroI2CDevice.class, I2CDevice.class})
public class DioZeroI2CDeviceTest {
	private DioZeroI2CDevice testDevice;

	private String name = "name";
	private int register = 0;
	private int address = 123;
	private int controller = 1;
	private long minimum = 0;
	private long maximum = 100;

	private ByteBuffer buffer = ByteBuffer.wrap(new byte[] {0x00, 0x01, 0x02});
	private byte b = 0x42;

	@Mock
	I2CDevice mockedDevice;

	@Before
	public void setUp() throws Exception {
		DioZeroI2CConfiguration config = DioZeroI2CConfiguration.builder().address(address).controller(controller)
				.max(maximum).min(minimum).name(name).register(register).build();
		whenNew(I2CDevice.class).withAnyArguments().thenReturn(mockedDevice);
		testDevice = new DioZeroI2CDevice(name, config);
	}

	@Test
	public void testGetAddress() {
		when(mockedDevice.getAddress()).thenReturn(address);

		assertEquals(address, testDevice.getAddress());
	}

	@Test
	public void testGetController() {
		when(mockedDevice.getController()).thenReturn(controller);

		assertEquals(controller, testDevice.getController());
	}

	@Test
	public void testGetName() {
		assertEquals(name, testDevice.getName());
	}

	@Test
	public void tesReadUInt() {
		long dataLong = 65000000L;
		when(mockedDevice.readUInt(eq(register))).thenReturn(dataLong);

		assertEquals(((dataLong - minimum) / (maximum - minimum)), testDevice.readUInt());
	}

	@Test
	public void testRead() {
		doReturn(buffer).when(mockedDevice).read(eq(register), eq(3));

		assertEquals(buffer, testDevice.read(3));
	}

	@Test
	public void testReadByte() {
		doReturn(b).when(mockedDevice).readByte(eq(register));

		assertEquals(b, testDevice.readByte());
	}

	@Test
	public void testWrite() {
		doNothing().when(mockedDevice).write(any(), any());

		testDevice.write(buffer);
		verify(mockedDevice).write(eq(buffer),eq(3));
		buffer.position(3);
		testDevice.write(buffer);
		verify(mockedDevice).write(eq(buffer),eq(0));
	}

	@Test
	public void testWriteByte() {
		doNothing().when(mockedDevice).writeByte(eq(b));

		testDevice.writeByte(b);
		verify(mockedDevice).writeByte(eq(b));
	}

	@Test
	public void testIsOpen() {
		when(mockedDevice.isOpen()).thenReturn(true);

		assertTrue(testDevice.isOpen());
	}

	@Test
	public void testClose() {
		doNothing().when(mockedDevice).close();

		testDevice.close();

		verify(mockedDevice).close();
	}
}
