package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfiguration;
import io.netty.channel.Channel;

import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.eclipse.neoscada.protocol.iec60870.client.ClientModule;
import org.eclipse.neoscada.protocol.iec60870.client.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Iec104ConnectionsFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ConnectionsFactory.class);

    private final Map<String, Iec104Cache> caches = new HashMap<>();
    private final Map<String, Iec104ClientModule> connections = new HashMap<>();
    private final Map<String, Integer> commonAddresses = new HashMap<>();
    private final List<Client> clients = new ArrayList<>();

    Iec104ConnectionsFactory()
    {
    }

    public Iec104ClientModule getConnection(String deviceId) {
        return connections.get(deviceId);
    }

    public Iec104Cache getCache(String deviceId) {
        return caches.get(deviceId);
    }

    public Integer getCommonAddress(String deviceId) {
        return commonAddresses.get(deviceId);
    }

    public List<String> getDeviceList() {
        return new ArrayList<>(caches.keySet());
    }

    public void createConnections (List<Iec104DatastreamsConfiguration> configuration) {
        disconnect();
        deleteConnections();
        HashMap<SocketAddress, List<ClientModule>> newConnections = new HashMap<>();

        ProtocolOptions.Builder optionsBuilder = new ProtocolOptions.Builder();
        optionsBuilder.setTimeout1(10000);
        optionsBuilder.setTimeout2(10000);
        optionsBuilder.setTimeout3(10000);
        ProtocolOptions options = optionsBuilder.build();

        configuration.forEach(c -> createNewConnection(c, newConnections, options));

        newConnections.entrySet().forEach(e -> {
            Client client = new Client(e.getKey(), new ConnectionStateListener() {

                @Override
                public void connected(Channel channel) {
                    LOGGER.info("Client connected {}", channel.remoteAddress());
                }

                @Override
                public void disconnected(Throwable e) {
                    LOGGER.error("Client disconnect", e);
                    LOGGER.error("Reconnecting...", e);
                    connect();
                }

            }, options, e.getValue());
            clients.add(client);
        });
    }

    private void createNewConnection (Iec104DatastreamsConfiguration currentConfiguration, HashMap<SocketAddress, List<ClientModule>> newConnections, ProtocolOptions options) {
        String deviceId = currentConfiguration.getDeviceId();
        String remoteAddress = currentConfiguration.getIpAddress();
        int port = currentConfiguration.getIpPort();
        int commonAddress = currentConfiguration.getCommonAddress();

        LOGGER.info("Creating IEC104 connections");

        try {
            InetAddress address = InetAddress.getByName(remoteAddress);
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);

            List<ClientModule> clientModules = newConnections.get(socketAddress);

            if (clientModules == null) {
                clientModules = new ArrayList<>();
                newConnections.put(socketAddress, clientModules);
            }
            clientModules.add(createClientModule(deviceId, options, commonAddress));
            LOGGER.info("Configured IEC104 client {} to {} at port:{}", deviceId, remoteAddress, port);
        } catch (IOException e) {
            LOGGER.error("Error creating IEC104 connection {}", e.getMessage());
        }
    }

    private Iec104ClientModule createClientModule (String deviceId, ProtocolOptions options, int commonAddress) {
        Iec104Cache cache = new Iec104Cache();
        Iec104ClientModule clientModule = new Iec104ClientModule(cache, options);

        caches.put(deviceId, cache);
        connections.put(deviceId, clientModule);
        commonAddresses.put(deviceId, commonAddress);

        return clientModule;
    }

    public void connect() {
        LOGGER.info("Establishing IEC104 connections");
        clients.forEach(c -> {
            ListenableFuture<Void> ret = c.connect();
            if (ret.isDone()) LOGGER.info("Client connected");
            else LOGGER.warn("Client not connected");
            });
    }

    public void disconnect() {
        LOGGER.info("Disconnecting old IEC104 connections");
        clients.forEach(c -> {
            try {
                c.close();
            } catch (Exception e) {
                LOGGER.error("Error closing connection {}", c);
            }
        });
    }

    public void deleteConnections() {
        connections.clear();
        caches.clear();
        commonAddresses.clear();
        clients.clear();
    }

}
