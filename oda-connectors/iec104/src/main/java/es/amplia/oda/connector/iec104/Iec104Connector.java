package es.amplia.oda.connector.iec104;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.slave.Iec104ServerModule;
import es.amplia.oda.connector.iec104.configuration.Iec104ConnectorConfiguration;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import io.netty.channel.socket.SocketChannel;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.apci.MessageChannel;
import org.eclipse.neoscada.protocol.iec60870.asdu.types.Value;
import org.eclipse.neoscada.protocol.iec60870.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;

public class Iec104Connector implements ScadaConnector, AutoCloseable{

	private static final Logger LOGGER = LoggerFactory.getLogger(Iec104Connector.class);

	private final Iec104Cache cache;
	private final ScadaDispatcherProxy dispatcher;

	Iec104ServerModule serverModule;
	Server server;
	SocketChannel socketChannel;
	MessageChannel messageChannel;
	private int commonAddress;
	private boolean spontaneousEnabled;


	Iec104Connector(Iec104Cache cache, ScadaDispatcherProxy dispatcher) {
		this.cache = cache;
		this.dispatcher = dispatcher;
	}

	@Override
	public <T, S> void uplink(int index, T value, S type, long timestamp) {
		String iec104Type = (String) type;

		Value<T> newValue = new Value<>(value, System.currentTimeMillis(), null);
		cache.add(iec104Type, newValue, index);

		if (spontaneousEnabled || Iec104Cache.isSpontaneous(iec104Type)) {
			if (isConnected()) {
				Object asdu = cache.getAsdu(iec104Type, value, index, timestamp, this.commonAddress);
				serverModule.send(asdu);
				LOGGER.debug("Uplink info to index {}", index);
			} else {
				LOGGER.warn("No connection established for sending data to Master SCADA. Message cannot be sent");
			}
		}
	}

	@Override
	public boolean isConnected() {
		return serverModule.isConnected();
	}

	public void loadConfiguration(Iec104ConnectorConfiguration currentConfiguration) {
		try {
			LOGGER.info("Loading configuration for IEC104 connector");
			clearLastConfiguration();
			InetAddress address = InetAddress.getByName(currentConfiguration.getLocalAddress());
			int port = currentConfiguration.getLocalPort();
			this.commonAddress = currentConfiguration.getCommonAddress();
			InetSocketAddress socketAddress = new InetSocketAddress(address, port);
			ProtocolOptions.Builder optionsBuilder = new ProtocolOptions.Builder();
			optionsBuilder.setTimeout1(15000);
			optionsBuilder.setTimeout2(10000);
			optionsBuilder.setTimeout3(10000);
			ProtocolOptions options = optionsBuilder.build();
			serverModule = new Iec104ServerModule(cache, options, dispatcher, commonAddress);
			if(this.socketChannel != null && this.messageChannel != null)
				serverModule.initializeChannel(this.socketChannel, this.messageChannel);
			this.server = new Server(socketAddress, options, Collections.singletonList(serverModule));
			this.spontaneousEnabled = currentConfiguration.isSpontaneousEnabled();
			LOGGER.info("Configured IEC104 server {} at port:{}", address, port);
		} catch (IOException e) {
			LOGGER.error("Error configuring connector");
		}
	}

	private void clearLastConfiguration() {
		LOGGER.info("Clearing previous IEC104 configuration");
		try {
			if (server != null) {
				serverModule.dispose();
				if(this.socketChannel == null && this.messageChannel == null) {
					this.socketChannel = serverModule.getSocketChannel();
					this.messageChannel = serverModule.getMessageChannel();
				}
				this.server.close();
				server = null;
			}
		} catch (Exception e) {
			LOGGER.error("Error erasing configuration of IEC 104 Connector", e);
		} finally {
			cache.clear();
		}
	}

	@Override
	public void close() {
		clearLastConfiguration();
		if(this.socketChannel != null) {
			this.socketChannel.close();
			this.socketChannel = null;
		}
		if(this.messageChannel != null) {
			this.messageChannel = null;
		}
	}
}
