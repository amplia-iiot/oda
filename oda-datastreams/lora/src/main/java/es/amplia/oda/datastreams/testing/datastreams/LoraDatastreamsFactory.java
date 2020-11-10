package es.amplia.oda.datastreams.testing.datastreams;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.udp.UdpService;

public class LoraDatastreamsFactory {
	private final UdpService udpService;
	private final EventPublisher publisher;
	private final Serializer serializer;

	public LoraDatastreamsFactory(UdpService udpService, EventPublisher publisher, Serializer serializer) {
		this.udpService = udpService;
		this.publisher = publisher;
		this.serializer = serializer;
	}

	public LoraDatastreamsEvent createLoraDatastreamsEvent(String deviceId) {
		return new LoraDatastreamsEvent(udpService, publisher, serializer, deviceId);
	}
}
