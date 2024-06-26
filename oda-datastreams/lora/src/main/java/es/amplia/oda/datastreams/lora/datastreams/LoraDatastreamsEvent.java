package es.amplia.oda.datastreams.lora.datastreams;

import es.amplia.oda.core.commons.interfaces.AbstractDatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.udp.UdpException;
import es.amplia.oda.core.commons.udp.UdpPacket;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.datastreams.lora.datastructures.LoraDataPacket;
import es.amplia.oda.datastreams.lora.datastructures.LoraStatusPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LoraDatastreamsEvent extends AbstractDatastreamsEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoraDatastreamsEvent.class);

	private final UdpService udpService;
	private final Serializer serializer;
	private final String deviceId;

	private Thread readingThread;

	public LoraDatastreamsEvent(UdpService udpService, EventPublisher publisher, Serializer serializer, String deviceId) {
		super(publisher);
		this.udpService = udpService;
		this.serializer = serializer;
		this.deviceId = deviceId;
	}

	@Override
	public void registerToEventSource() {
		readingThread = new Thread(() -> {
			try {
				while (!readingThread.isInterrupted()) {
					readChannel();
				}
			} catch (UdpException e) {
				LOGGER.error("Error trying to read the udp channel: {}", e.getMessage());
			}
		});
		readingThread.start();
	}

	private void readChannel() {
		try {
			if (udpService.isBound()) {
				CompletableFuture<UdpPacket> futurePacket = udpService.receiveMessage();
				/* First 40 bytes are:
				  		- 20 octets: IP Header.
				  		- 8  octets: UDP header.
				  		- 12 octets: RTP header.
				  	Actually we don't want to use this info and it will be skipped
				 */
				UdpPacket udpPacket = futurePacket.get();
				byte[] data = Arrays.copyOfRange(udpPacket.getDataAsBytes(), 12, udpPacket.getDataAsBytes().length);

				tryDeserializeAsLoraStatus(data);
				tryDeserializeAsLoraData(data);
			} else {
				TimeUnit.SECONDS.sleep(10);
			}
		} catch (InterruptedException e) {
			LOGGER.warn("Thread interrupted while reading {}", e.getMessage());
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			LOGGER.warn("Skipping last received message. Was impossible to receive it well: {}", e.getMessage());
		}
	}

	private void tryDeserializeAsLoraStatus(byte[] data) {
		try {
			LoraStatusPacket loraStayAlive = serializer.deserialize(data, LoraStatusPacket.class);
			if(loraStayAlive != null && loraStayAlive.getStat() != null) {
				Map<String, Map<String, Map<Long, Object>>> events = new HashMap<>();
				Map<String, Map<Long, Object>> event = new HashMap<>();
				Map<Long, Object> eventData = new HashMap<>();

				eventData.put(System.currentTimeMillis(), loraStayAlive);
				event.put(null, eventData);
				events.put("lora", event);
				this.publish(deviceId, null, events);
				LOGGER.info("Sent LoRa status message at: {}", System.currentTimeMillis());
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("LoRa packet content: {}", loraStayAlive.toShortString());
				}
			} else {
				throw new IOException();
			}
		} catch (IOException | AssertionError e) {
			LOGGER.warn("Packet was not a LoRa StayAlive signal");
		}
	}

	private void tryDeserializeAsLoraData(byte[] data) {
		try {
			LoraDataPacket loraPacket = serializer.deserialize(data, LoraDataPacket.class);
			if(loraPacket != null && loraPacket.getRxpk() != null) {

				Map<String, Map<String, Map<Long, Object>>> events = new HashMap<>();
				Map<String, Map<Long, Object>> event = new HashMap<>();
				Map<Long, Object> eventData = new HashMap<>();
				eventData.put(System.currentTimeMillis(), loraPacket);
				event.put(null, eventData);
				events.put("lora", event);
				this.publish(deviceId, null, events);
				LOGGER.info("Sent LoRa data message");
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("LoRa packet content: {}", loraPacket.toShortString());
				}
			} else {
				throw new IOException();
			}
		} catch (IOException e) {
			LOGGER.warn("Packet was not a LoRa Packet");
		}
	}

	@Override
	public void unregisterFromEventSource() {
		this.readingThread.interrupt();
	}
}
