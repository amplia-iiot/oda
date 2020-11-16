package es.amplia.oda.core.commons.udp;

import java.util.concurrent.CompletableFuture;

public interface UdpService {

	CompletableFuture<UdpPacket> receiveMessage();

	void stop();

	void sendMessage(byte[] message);

	void loadConfiguration(String host, int uplinkPort, int downlinkPort, int packetSize);

	boolean isBound();
}
