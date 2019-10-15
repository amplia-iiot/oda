package es.amplia.oda.connector.iec104;

import es.amplia.oda.connector.iec104.configuration.Iec104ConnectorConfiguration;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;

import io.netty.channel.socket.SocketChannel;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
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

	private Iec104ServerModule serverModule;
	private Server server;
	private SocketChannel socketChannel;
	private int commonAddress;
	private boolean spontaneousEnabled;


	Iec104Connector(Iec104Cache cache, ScadaDispatcherProxy dispatcher) {
		this.cache = cache;
		this.dispatcher = dispatcher;
	}

	@Override
	public <T, S> void uplink(int index, T value, S type, long timestamp) {
		String iec104Type = (String) type;

		cache.add(iec104Type, value, index);

		if (spontaneousEnabled || Iec104Cache.isSpontaneous(iec104Type)) {
			if (isConnected()) {
				Object asdu = cache.getAsdu(iec104Type, value, index, timestamp, this.commonAddress);
				serverModule.send(asdu);
			} else {
				LOGGER.info("No connection established for sending data to Master SCADA");
			}
		}
	}

	@Override
	public boolean isConnected() {
		return serverModule.isConnected();
	}

	public void loadConfiguration(Iec104ConnectorConfiguration currentConfiguration) {
		try {
			clearLastConfiguration();
			InetAddress address = InetAddress.getByName(currentConfiguration.getLocalAddress());
			int port = currentConfiguration.getLocalPort();
			this.commonAddress = currentConfiguration.getCommonAddress();
			InetSocketAddress socketAddress = new InetSocketAddress(address, port);
			ProtocolOptions.Builder optionsBuilder = new ProtocolOptions.Builder();
			optionsBuilder.setTimeout1(10000);
			optionsBuilder.setTimeout2(10000);
			optionsBuilder.setTimeout3(10000);
			ProtocolOptions options = optionsBuilder.build();
			serverModule = new Iec104ServerModule(cache, options, dispatcher, commonAddress);
			this.server = new ServerKeepAlive(socketAddress, options, Collections.singletonList(serverModule), dispatcher, commonAddress);
			this.spontaneousEnabled = currentConfiguration.isSpontaneousEnabled();
			LOGGER.info("Configured IEC104 server {} at port:{}", address, port);
		} catch (IOException e) {
			LOGGER.error("Error configuring connector");
		}
	}

	private void clearLastConfiguration() {
		try {
			if (server != null) {
				serverModule.dispose();
				this.socketChannel.close();
				this.server.close();
				server = null;
				socketChannel = null;
			}
		} catch (Exception e) {
			LOGGER.error("Error erasing configuration of IEC 104 Connector");
		} finally {
			cache.clear();
		}
	}

	@Override
	public void close() {
		clearLastConfiguration();
	}
}
