package es.amplia.oda.connector.iec104;

import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.server.Server;
import org.eclipse.neoscada.protocol.iec60870.server.ServerModule;
import java.net.SocketAddress;
import java.util.List;

@Deprecated
public class ServerKeepAlive extends Server {

	ServerKeepAlive(SocketAddress address, ProtocolOptions options, List<ServerModule> modules, ScadaDispatcherProxy dispatcher, int commonAddress) {
		super(address, options, modules);
	}
}
