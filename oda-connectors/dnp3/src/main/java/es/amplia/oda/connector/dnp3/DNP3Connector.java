package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.*;
import com.automatak.dnp3.enums.IndexMode;
import com.automatak.dnp3.impl.DNP3ManagerFactory;
import com.automatak.dnp3.mock.DefaultOutstationApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.connector.dnp3.configuration.DNP3ConnectorConfiguration;

public class DNP3Connector implements ScadaConnector, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DNP3Connector.class);


    private final ScadaTableInfo tableInfo;
    private final ScadaDispatcher dispatcher;
    private final DNP3Manager manager;
    private final ServiceRegistrationManager<ScadaConnector> scadaConnectorRegistrationManager;

    private DNP3ChannelListener channelListener;
    private String outstationIdentifier;
    private OutstationStackConfig outstationStackConfig;
    private Channel channel;
    private Outstation outstation;

    DNP3Connector(ScadaTableInfo tableInfo, ScadaDispatcher dispatcher,
                         ServiceRegistrationManager<ScadaConnector> scadaConnectorRegistrationManager)
            throws DNP3Exception {
        this.tableInfo = tableInfo;
        this.dispatcher = dispatcher;
        this.scadaConnectorRegistrationManager = scadaConnectorRegistrationManager;
        manager = DNP3ManagerFactory.createManager(new DNP3LogHandler());
    }

    public void loadConfiguration(DNP3ConnectorConfiguration config) throws DNP3Exception {
        LOGGER.info("Loading last configuration for DNP3 connector");
        clearLastConfiguration();

        channelListener = new DNP3ChannelListener();
        channel = manager.addTCPServer(config.getChannelIdentifier(), config.getLogLevel(), ChannelRetry.getDefault(),
                                       config.getIpAddress(), config.getIpPort(), channelListener);

        outstationIdentifier = config.getOutstationIdentifier();
        DatabaseConfig databaseConfig = createDatabaseConfig();
        EventBufferConfig eventBufferConfig = EventBufferConfig.allTypes(config.getEventBufferSize());
        outstationStackConfig = new OutstationStackConfig(databaseConfig, eventBufferConfig);
        outstationStackConfig.linkConfig.localAddr = config.getLocalDeviceDNP3Address();
        outstationStackConfig.linkConfig.remoteAddr = config.getRemoteDeviceDNP3Address();
        outstationStackConfig.outstationConfig.indexMode = IndexMode.Discontiguous;
        outstationStackConfig.outstationConfig.allowUnsolicited = config.isUnsolicitedResponse();
        LOGGER.info("Last configuration for DNP3 connector loaded");
    }

    public void init() throws DNP3Exception {
        CommandHandler commandHandler = new ScadaCommandHandler(dispatcher);
        OutstationApplication outstationApplication = DefaultOutstationApplication.getInstance();

        outstation = channel.addOutstation(outstationIdentifier, commandHandler, outstationApplication,
                outstationStackConfig);

        outstation.enable();
        scadaConnectorRegistrationManager.register(this);
    }

    private void clearLastConfiguration() {
        LOGGER.info("Shutting down previous connector");
        if (outstation != null) {
            scadaConnectorRegistrationManager.unregister();
            outstation.shutdown();
            outstation = null;
        }
        if (channel != null) {
            channel.shutdown();
            channel = null;
        }
        channelListener = null;
    }

    private DatabaseConfig createDatabaseConfig() {
        return new DatabaseConfig(tableInfo.getNumBinaryInputs(), tableInfo.getNumDoubleBinaryInputs(),
                tableInfo.getNumAnalogInputs(), tableInfo.getNumCounters(), tableInfo.getNumFrozenCounters(),
                tableInfo.getNumBinaryOutputs(), tableInfo.getNumAnalogOutputs());
    }

    @Override
    public <T, S> void uplink(int index, T value, S type, long timestamp) {
        if (!isConnected()) {
            LOGGER.warn("Can not uplink data: DNP 3.0 connector is not connected");
            return;
        }

        OutstationChangeSet updateSet = new OutstationChangeSet();
        byte dataQuality = 0x01;

        if (isBinaryOutput(value)) {
            LOGGER.info("Uplink binary input in index {}", index);
            LOGGER.debug("Binary value {} sent in index {}", Boolean.parseBoolean(value.toString()), index);
            BinaryInput binaryInput = new BinaryInput(Boolean.parseBoolean(value.toString()), dataQuality, timestamp);
            updateSet.update(binaryInput, index);
        } else if (isAnalogInput(value)) {
            LOGGER.info("Uplink analog input in index {}", index);
            LOGGER.debug("Analog value {} sent in index {}", Double.parseDouble(value.toString()), index);
            AnalogInput analogInput = new AnalogInput(Double.parseDouble(value.toString()), dataQuality, timestamp);
            updateSet.update(analogInput, index);
        } else {
            LOGGER.warn("Can not uplink data: Unknown type");
            return;
        }

        outstation.apply(updateSet);
    }

    @Override
    public boolean isConnected() {
        return channelListener != null && channelListener.isOpen();
    }

    private <T> boolean isBinaryOutput(T value) {
        return value instanceof Boolean || (value instanceof String &&
                (value.equals(Boolean.TRUE.toString()) || value.equals(Boolean.FALSE.toString())));
    }

    private <T> boolean isAnalogInput(T value) {
        return value instanceof Integer || (value instanceof Double) ||
                (value instanceof String && tryParseDouble((String) value));

    }

    private boolean tryParseDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void close() {
        clearLastConfiguration();
        manager.shutdown();
    }
}
