package es.amplia.oda.hardware.udp.udp;

import es.amplia.oda.core.commons.udp.UdpException;
import es.amplia.oda.core.commons.udp.UdpPacket;
import es.amplia.oda.core.commons.udp.UdpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.CompletableFuture;

public class JavaUdpService implements UdpService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JavaUdpService.class);

	private DatagramSocket socket;

	private int packetSize;

	@Override
	public CompletableFuture<UdpPacket> receiveMessage() {
		return CompletableFuture.supplyAsync(() -> {
			JavaUdpPacket datagram = new JavaUdpPacket(new byte[packetSize]);
			try {
				socket.receive(datagram.getDatagramPacket());
				LOGGER.debug("Udp packet received to the ODA server");
			} catch (IOException e) {
				LOGGER.warn("Couldn't receive the udp packet from the server");
				throw new UdpException(e.getMessage());
			}
			return datagram;
		});
	}

	@Override
	public void sendMessage(byte[] message) {
		LOGGER.info("Sending message to udp client");
		try {
			DatagramPacket toSendPacket = new DatagramPacket(message, 0, message.length);
			socket.send(toSendPacket);
			LOGGER.info("Message sent correctly through udp to udp device");
			LOGGER.debug("Message sent {}", toSendPacket.getData());
		} catch (IOException e) {
			LOGGER.warn("Couldn't send message to udp client where the socket is connected");
			throw new UdpException(e.getMessage());
		}
	}

	@Override
	public void loadConfiguration(String host, int uplinkPort, int downlinkPort, int packetSize) {
		try {
			stop();
			socket = new DatagramSocket(uplinkPort, InetAddress.getByName(host));
//			socket.connect(InetAddress.getByName(host), downlinkPort);
			LOGGER.info("Udp connection achieved in {}:{}. It will send messages in {}:{}",
					host, uplinkPort, host, downlinkPort);
		} catch (SocketException | UnknownHostException e) {
			LOGGER.error("Couldn't achieve the connection through udp with : {}:{}", host, uplinkPort);
		}
		this.packetSize = packetSize;
	}

	@Override
	public boolean isBound() {
		return this.socket != null && this.socket.isBound();
	}

	@Override
	public void stop() {
		if (socket != null) {
			socket.disconnect();
			socket.close();
		}
		LOGGER.info("UDP connection closed");
	}
}