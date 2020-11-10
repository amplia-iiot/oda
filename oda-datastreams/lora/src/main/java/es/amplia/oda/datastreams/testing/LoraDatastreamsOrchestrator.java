package es.amplia.oda.datastreams.testing;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.datastreams.testing.configuration.LoraDatastreamsConfiguration;
import es.amplia.oda.datastreams.testing.datastreams.LoraDatastreamsEvent;
import es.amplia.oda.datastreams.testing.datastreams.LoraDatastreamsFactory;
import es.amplia.oda.datastreams.testing.datastreams.LoraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoraDatastreamsOrchestrator implements AutoCloseable{

	private static final Logger LOGGER = LoggerFactory.getLogger(LoraDatastreamsOrchestrator.class);

	private final UdpService udpService;
	private final EventPublisher publisher;
	private final Serializer serializer;

	private LoraDatastreamsEvent loraDatastreamsEvent;

	public LoraDatastreamsOrchestrator(UdpService udpService, EventPublisher publisher, Serializer serializer) {
		this.udpService = udpService;
		this.publisher = publisher;
		this.serializer = serializer;
	}

	public void loadConfiguration(LoraDatastreamsConfiguration configuration) {
		closeResources();
		LoraDatastreamsFactory factory = new LoraDatastreamsFactory(udpService, publisher, serializer);
		loraDatastreamsEvent = factory.createLoraDatastreamsEvent(configuration.getDeviceId());
	}

	private void closeResources() {
		try {
			if (loraDatastreamsEvent != null) {
				loraDatastreamsEvent.unregisterFromEventSource();
				loraDatastreamsEvent = null;
			}
		} catch (LoraException e) {
			LOGGER.warn("Error closing LoRa resources {0}", e);
		}
	}

	@Override
	public void close() {
		closeResources();
	}
}
