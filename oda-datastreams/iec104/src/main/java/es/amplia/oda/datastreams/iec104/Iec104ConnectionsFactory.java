package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.configuration.Iec104DatastreamsConfiguration;
import es.amplia.oda.event.api.EventDispatcher;
import io.netty.channel.Channel;

import org.eclipse.neoscada.protocol.iec60870.ProtocolOptions;
import org.eclipse.neoscada.protocol.iec60870.client.Client;
import org.eclipse.neoscada.protocol.iec60870.client.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.ZoneId;
import java.util.*;

public class Iec104ConnectionsFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104ConnectionsFactory.class);

    private final Map<String, Iec104Cache> caches = new HashMap<>();
    private final Map<String, Iec104ClientModule> connections = new HashMap<>();
    private final Map<String, Integer> commonAddresses = new HashMap<>();
    private final List<Client> clients = new ArrayList<>();

    private final EventDispatcher eventDispatcher;
    private final ScadaTableTranslator scadaTables;


    Iec104ConnectionsFactory(EventDispatcher eventDispatcher, ScadaTableTranslator scadaTables)
    {
        this.eventDispatcher = eventDispatcher;
        this.scadaTables = scadaTables;
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

    public List<String> getConnectionsDeviceList() {
        return new ArrayList<>(connections.keySet());
    }

    public void createConnections (List<Iec104DatastreamsConfiguration> configuration) {
        disconnect();
        deleteConnections();
        HashMap<SocketAddress, List<Iec104ClientModule>> newConnections = new HashMap<>();

        // create caches for every device in scada tables and connections
        createCaches(configuration, scadaTables.getRecollectionDeviceIds());

        // set protocol options
        ProtocolOptions.Builder optionsBuilder = new ProtocolOptions.Builder();
        optionsBuilder.setTimeout1(15000);
        optionsBuilder.setTimeout2(10000);
        optionsBuilder.setTimeout3(10000);
        optionsBuilder.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        ProtocolOptions options = optionsBuilder.build();

        // create connection
        configuration.forEach(c -> createNewConnection(c, newConnections, options));

        newConnections.entrySet().forEach(e -> {
            Client client = new Client(e.getKey(), new ConnectionStateListener() {

                @Override
                public void connected(Channel channel) {
                    LOGGER.info("Client connected {}", channel.remoteAddress());
                    e.getValue().forEach(client ->
                    {
                        LOGGER.info("DeviceId {} connected ", client.getDeviceId());
                        client.setConnected(true);
                    });
                }

                @Override
                public void disconnected(Throwable error) {
                    LOGGER.error("Client disconnect", error);
                    e.getValue().forEach(client ->
                    {
                        LOGGER.info("DeviceId {} disconnected ", client.getDeviceId());
                        client.setConnected(false);
                    });

                 /*   LOGGER.error("Reconnecting...", e);
                    connect();*/
                }

            }, options, new ArrayList<>(e.getValue()));
            clients.add(client);
        });
    }

    private void createNewConnection (Iec104DatastreamsConfiguration currentConfiguration, HashMap<SocketAddress,
            List<Iec104ClientModule>> newConnections, ProtocolOptions options) {
        String deviceId = currentConfiguration.getDeviceId();
        String remoteAddress = currentConfiguration.getIpAddress();
        int port = currentConfiguration.getIpPort();
        int commonAddress = currentConfiguration.getCommonAddress();

        LOGGER.info("Creating IEC104 connections");

        try {
            InetAddress address = InetAddress.getByName(remoteAddress);
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);

            List<Iec104ClientModule> clientModules = newConnections.get(socketAddress);

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
        Iec104ClientModule clientModule = new Iec104ClientModule(caches, options, deviceId, commonAddress,
                this.eventDispatcher, this.scadaTables);

        connections.put(deviceId, clientModule);
        commonAddresses.put(deviceId, commonAddress);

        return clientModule;
    }

    public void connect() {
        LOGGER.info("Establishing IEC104 connections");
        clients.forEach(c -> {
            ListenableFuture<Void> ret = c.connect();
            if (ret.isDone()) {
                LOGGER.info("Client connected");
            } else {
                LOGGER.warn("Client not connected");
            }
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
        commonAddresses.clear();
        clients.clear();
    }

    private void createCaches(List<Iec104DatastreamsConfiguration> configuration, List<String> signalsDeviceIds)
    {
        // clear map
        caches.clear();

        if(signalsDeviceIds != null) {
            for (String signalsDeviceId : signalsDeviceIds) {
                caches.put(signalsDeviceId, new Iec104Cache());
            }
        }

        // add the deviceIds of the IEC104 connections to the caches
        // signals without deviceId registered will be assigned to these caches
        if(configuration != null) {
            for (Iec104DatastreamsConfiguration connectionData : configuration) {
                caches.put(connectionData.getDeviceId(), new Iec104Cache());
            }
        }
    }
}
