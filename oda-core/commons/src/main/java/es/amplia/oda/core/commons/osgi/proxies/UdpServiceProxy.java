package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.udp.UdpException;
import es.amplia.oda.core.commons.udp.UdpPacket;
import es.amplia.oda.core.commons.udp.UdpService;
import org.osgi.framework.BundleContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UdpServiceProxy implements UdpService, AutoCloseable {

	private static final String NO_UDP_SERVICE_AVAILABLE_MESSAGE = "No UDP service available";

	private final OsgiServiceProxy<UdpService> proxy;

	public UdpServiceProxy(BundleContext bundleContext) {
		this.proxy = new OsgiServiceProxy<>(UdpService.class, bundleContext);
	}

	@Override
	public CompletableFuture<UdpPacket> receiveMessage() {
		return Optional.ofNullable(proxy.callFirst(UdpService::receiveMessage))
				.orElseThrow(() -> new UdpException(NO_UDP_SERVICE_AVAILABLE_MESSAGE));
	}

	@Override
	public void stop() {
		proxy.consumeFirst(UdpService::stop);
	}

	@Override
	public void sendMessage(byte[] message) {
		proxy.consumeFirst(udpService -> udpService.sendMessage(message));
	}

	@Override
	public void loadConfiguration(String host, int uplinkPort, int downlinkPort, int packetSize) {
		proxy.consumeFirst(udpService -> udpService.loadConfiguration(host, uplinkPort, downlinkPort, packetSize));
	}

	@Override
	public boolean isBound() {
		return Optional.ofNullable(proxy.callFirst(UdpService::isBound))
				.orElseThrow(() -> new UdpException(NO_UDP_SERVICE_AVAILABLE_MESSAGE));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
