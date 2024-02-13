package es.amplia.oda.hardware.udp.udp;

import es.amplia.oda.core.commons.udp.UdpException;
import es.amplia.oda.core.commons.udp.UdpPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaUdpService.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class JavaUdpServiceTest {

	private static final int PACKET_SIZE_TEST_VALUE = 512;

	@Mock
	DatagramSocket mockedSocket;
	@InjectMocks
	private static final JavaUdpService testService = new JavaUdpService();

	@Mock
	JavaUdpPacket mockedPacket;
	@Mock
	DatagramPacket mockedDatagramPacket;

	@Before
	public void setUp() {
		Whitebox.setInternalState(testService, "packetSize", PACKET_SIZE_TEST_VALUE);
	}

	@Test
	public void testReceiveMessage() throws Exception {
		doNothing().when(mockedSocket).receive(any());
		whenNew(JavaUdpPacket.class).withAnyArguments().thenReturn(mockedPacket);

		CompletableFuture<UdpPacket> futurePacket = testService.receiveMessage();

		assertNotNull(futurePacket);
		UdpPacket udpPacket = futurePacket.get();
		assertEquals(this.mockedPacket, udpPacket);

	}

	@Test(expected = ExecutionException.class)
	public void testReceiveMessageExceptionOnReceive() throws Exception {
		doThrow(new IOException()).when(mockedSocket).receive(any());

		CompletableFuture<UdpPacket> packet = testService.receiveMessage();

		packet.get();
	}

	@Test
	public void testSendMessage() throws Exception {
		byte[] testBytes= {0x00, 0x01, 0x02, 0x03, 0x04};
		whenNew(DatagramPacket.class).withAnyArguments().thenReturn(mockedDatagramPacket);

		testService.sendMessage(testBytes);

		verifyNew(DatagramPacket.class).withArguments(eq(testBytes), eq(0), eq(testBytes.length));
		verify(mockedSocket).send(mockedDatagramPacket);
	}

	@Test(expected = UdpException.class)
	public void testSendMessageExceptionOnReceive() throws Exception {
		byte[] testBytes= {0x00, 0x01, 0x02, 0x03, 0x04};
		doThrow(new IOException()).when(mockedSocket).send(any());

		testService.sendMessage(testBytes);
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		String testHost = "localhost";
		int uplink = 1008;
		int downlink = 1002;
		int packetSize = 2048;
		whenNew(DatagramSocket.class).withAnyArguments().thenReturn(mockedSocket);

		testService.loadConfiguration(testHost, uplink, downlink, packetSize);

		verifyNew(DatagramSocket.class).withArguments(eq(uplink), eq(InetAddress.getByName(testHost)));
//		verify(mockedSocket).connect(any(), eq(downlink));
		assertEquals(packetSize, (int) Whitebox.getInternalState(testService, "packetSize"));
	}

	@Test
	public void testLoadConfigurationException() {
		String testHost = "thisIsAnUnknownHost";
		int uplink = 1008;
		int downlink = 1002;
		int packetSize = 2048;

		testService.loadConfiguration(testHost, uplink, downlink, packetSize);

		verify(mockedSocket, never()).connect(any(), eq(downlink));
	}

	@Test
	public void testBoundTrue() {
		when(mockedSocket.isBound()).thenReturn(true);

		boolean bound = testService.isBound();

		assertTrue(bound);
	}

	@Test
	public void testBoundFalse() {
		when(mockedSocket.isBound()).thenReturn(false);

		boolean bound = testService.isBound();

		assertFalse(bound);
	}

	@Test
	public void testStop() {
		testService.stop();

		verify(mockedSocket).disconnect();
		verify(mockedSocket).close();
	}
}