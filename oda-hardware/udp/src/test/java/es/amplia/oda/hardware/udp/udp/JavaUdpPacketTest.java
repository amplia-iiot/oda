package es.amplia.oda.hardware.udp.udp;

import es.amplia.oda.hardware.udp.Activator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class JavaUdpPacketTest {

	private static final byte[] DATA_BYTES = {
			0x45, 0x00, 0x01, 0x24, 0x18, 0x0b, 0x40, 0x00, 0x40, 0x11, 0x23, (byte) 0xbc, 0x7f, 0x00, 0x00, 0x01, 0x7f,
			0x00, 0x00, 0x01, (byte) 0xd7, (byte) 0xa6, 0x06, (byte) 0x95, 0x01, 0x10, (byte) 0xff, 0x23, 0x01,
			(byte) 0xdd, (byte) 0xaf, 0x00, 0x00, (byte) 0xe0, 0x4c, (byte) 0xff, (byte) 0xfe, 0x53, 0x44, 0x72, 0x7b,
			0x22, 0x72, 0x78, 0x70, 0x6b, 0x22, 0x3a, 0x5b, 0x7b, 0x22, 0x74, 0x6d, 0x73, 0x74, 0x22, 0x3a, 0x33, 0x32,
			0x36, 0x37, 0x33, 0x34, 0x32, 0x38, 0x39, 0x32, 0x2c, 0x22, 0x74, 0x69, 0x6d, 0x65, 0x22, 0x3a, 0x22, 0x32,
			0x30, 0x32, 0x30, 0x2d, 0x31, 0x31, 0x2d, 0x30, 0x36, 0x54, 0x31, 0x30, 0x3a, 0x31, 0x39, 0x3a, 0x34, 0x37,
			0x2e, 0x37, 0x39, 0x30, 0x30, 0x33, 0x30, 0x5a, 0x22, 0x2c, 0x22, 0x63, 0x68, 0x61, 0x6e, 0x22, 0x3a, 0x31,
			0x2c, 0x22, 0x72, 0x66, 0x63, 0x68, 0x22, 0x3a, 0x31, 0x2c, 0x22, 0x66, 0x72, 0x65, 0x71, 0x22, 0x3a, 0x34,
			0x33, 0x33, 0x2e, 0x33, 0x37, 0x35, 0x30, 0x30, 0x30, 0x2c, 0x22, 0x73, 0x74, 0x61, 0x74, 0x22, 0x3a, 0x31,
			0x2c, 0x22, 0x6d, 0x6f, 0x64, 0x75, 0x22, 0x3a, 0x22, 0x4c, 0x4f, 0x52, 0x41, 0x22, 0x2c, 0x22, 0x64, 0x61,
			0x74, 0x72, 0x22, 0x3a, 0x22, 0x53, 0x46, 0x31, 0x32, 0x42, 0x57, 0x31, 0x32, 0x35, 0x22, 0x2c, 0x22, 0x63,
			0x6f, 0x64, 0x72, 0x22, 0x3a, 0x22, 0x34, 0x2f, 0x35, 0x22, 0x2c, 0x22, 0x6c, 0x73, 0x6e, 0x72, 0x22, 0x3a,
			0x35, 0x2e, 0x35, 0x2c, 0x22, 0x72, 0x73, 0x73, 0x69, 0x22, 0x3a, 0x2d, 0x39, 0x39, 0x2c, 0x22, 0x73, 0x69,
			0x7a, 0x65, 0x22, 0x3a, 0x33, 0x37, 0x2c, 0x22, 0x64, 0x61, 0x74, 0x61, 0x22, 0x3a, 0x22, 0x51, 0x4d, 0x41,
			0x32, 0x22, 0x7d, 0x5d, 0x7d
	};

	private JavaUdpPacket testPacket;

	@Before
	public void setUp() {
		testPacket = new JavaUdpPacket(DATA_BYTES);
	}

	@Test
	public void testGetDataAsBytes() {
		byte[] dataBytes = testPacket.getDataAsBytes();

		assertEquals(DATA_BYTES, dataBytes);
	}

	@Test
	public void testGetDataAsString() {
		String data = testPacket.getDataAsString();

		assertEquals(new String(DATA_BYTES, 0, DATA_BYTES.length), data);
	}

	@Test
	public void testSetDataAsBytes() {
		byte[] toChangeData = {0x00, 0x00};

		testPacket.setData(toChangeData);

		DatagramPacket packet = Whitebox.getInternalState(testPacket, "packet");
		assertEquals(toChangeData, packet.getData());
	}

	@Test
	public void testSetDataAsString() {
		byte[] toChangeData = {0x00, 0x00};
		String data = new String(toChangeData, 0, toChangeData.length);

		testPacket.setData(data);

		DatagramPacket packet = Whitebox.getInternalState(testPacket, "packet");
		assertEquals(data, new String(packet.getData(), 0, packet.getData().length));
	}

	@Test
	public void testGetAddress() {
		assertNull(testPacket.getAddress());
	}

	@Test
	public void testGetPort() {
		assertEquals(-1, testPacket.getPort());
	}

	@Test
	public void testSetAddress() throws UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();

		testPacket.setAddress(address);

		DatagramPacket packet = Whitebox.getInternalState(testPacket, "packet");
		assertEquals(address, packet.getAddress());
	}

	@Test
	public void testSetPort() throws UnknownHostException {
		int port = 4000;

		testPacket.setPort(port);

		DatagramPacket packet = Whitebox.getInternalState(testPacket, "packet");
		assertEquals(port, packet.getPort());
	}

	@Test
	public void testGetDatagramPacket() throws UnknownHostException {
		DatagramPacket expectedPacket = new DatagramPacket(DATA_BYTES, 0, DATA_BYTES.length);

		DatagramPacket packet = testPacket.getDatagramPacket();

		assertEquals(expectedPacket.getData(), packet.getData());
		assertEquals(expectedPacket.getAddress(), packet.getAddress());
		assertEquals(expectedPacket.getLength(), packet.getLength());
		assertEquals(expectedPacket.getPort(), packet.getPort());
		assertEquals(expectedPacket.getOffset(), packet.getOffset());
	}
}
