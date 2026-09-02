package es.amplia.oda.hardware.udp.udp;

import es.amplia.oda.core.commons.udp.UdpException;
import es.amplia.oda.core.commons.udp.UdpPacket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JavaUdpServiceTest {

	private static final int PACKET_SIZE_TEST_VALUE = 512;

	@Mock
	DatagramSocket mockedSocket;
	@InjectMocks
	private static final JavaUdpService testService = new JavaUdpService();

	@BeforeEach
	public void setUp() {
		Whitebox.setInternalState(testService, "packetSize", PACKET_SIZE_TEST_VALUE);
	}

	@Test
	public void testReceiveMessage() throws Exception {
		doNothing().when(mockedSocket).receive(any());

		CompletableFuture<UdpPacket> futurePacket = testService.receiveMessage();

		assertNotNull(futurePacket);
		UdpPacket udpPacket = futurePacket.get();
		assertTrue(udpPacket instanceof JavaUdpPacket);
		assertEquals(PACKET_SIZE_TEST_VALUE, udpPacket.getDataAsBytes().length);
		verify(mockedSocket).receive(any(DatagramPacket.class));
	}

	@Test
	public void testReceiveMessageExceptionOnReceive() throws Exception {
		doThrow(new IOException()).when(mockedSocket).receive(any());

		CompletableFuture<UdpPacket> packet = testService.receiveMessage();

		assertThrows(ExecutionException.class, () -> packet.get());
	}

	@Test
	public void testSendMessage() throws Exception {
		byte[] testBytes= {0x00, 0x01, 0x02, 0x03, 0x04};

		testService.sendMessage(testBytes);

		ArgumentCaptor<DatagramPacket> packetCaptor = ArgumentCaptor.forClass(DatagramPacket.class);
		verify(mockedSocket).send(packetCaptor.capture());
		assertEquals(testBytes, packetCaptor.getValue().getData());
		assertEquals(0, packetCaptor.getValue().getOffset());
		assertEquals(testBytes.length, packetCaptor.getValue().getLength());
	}

	@Test
	public void testSendMessageExceptionOnReceive() throws Exception {
		byte[] testBytes= {0x00, 0x01, 0x02, 0x03, 0x04};
		doThrow(new IOException()).when(mockedSocket).send(any());

		assertThrows(UdpException.class, () -> testService.sendMessage(testBytes));
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		String testHost = "localhost";
		int uplink = 1008;
		int downlink = 1002;
		int packetSize = 2048;
		List<List<?>> socketArgs = new ArrayList<>();

		try (MockedConstruction<DatagramSocket> socketCons = mockConstruction(DatagramSocket.class,
				(mock, mctx) -> socketArgs.add(new ArrayList<>(mctx.arguments())))) {
			testService.loadConfiguration(testHost, uplink, downlink, packetSize);

			assertEquals(1, socketCons.constructed().size());
			assertEquals(uplink, socketArgs.get(0).get(0));
			assertEquals(InetAddress.getByName(testHost), socketArgs.get(0).get(1));
		}
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
