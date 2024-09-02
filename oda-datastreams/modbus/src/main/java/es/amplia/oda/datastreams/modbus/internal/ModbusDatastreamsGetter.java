package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.modbus.ModbusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

class ModbusDatastreamsGetter implements DatastreamsGetter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusDatastreamsGetter.class);

    private final String datastreamId;
    private final Type datastreamType;
    private final Map<String, Integer> deviceIdSlaveAddressMapper;
    private final ModbusType dataType;
    private final int dataAddress;
    private final boolean readFromCache;
    private final int numRegistersToRead;
    private final ModbusReadOperatorProcessor readOperatorProcessor;


    ModbusDatastreamsGetter(String datastreamId, Type datastreamType, Map<String, Integer> deviceIdSlaveAddressMapper,
                            ModbusType dataType, int dataAddress, boolean readFromCache, int numRegistersToRead,
                            ModbusReadOperatorProcessor readOperatorProcessor) {
        this.datastreamId = datastreamId;
        this.datastreamType = datastreamType;
        this.deviceIdSlaveAddressMapper = deviceIdSlaveAddressMapper;
        this.dataType = dataType;
        this.dataAddress = dataAddress;
        this.readFromCache = readFromCache;
        this.numRegistersToRead = numRegistersToRead;
        this.readOperatorProcessor = readOperatorProcessor;
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return new ArrayList<>(deviceIdSlaveAddressMapper.keySet());
    }

    @Override
    public CompletableFuture<CollectedValue> get(String device) {
        int slaveAddress = Optional.ofNullable(deviceIdSlaveAddressMapper.get(device))
                .orElseThrow(() -> new IllegalArgumentException("Unknown device"));
        LOGGER.debug("Getting value from the datastream {} of the the device {}", datastreamId, device);
        return CompletableFuture.supplyAsync(() ->
                readOperatorProcessor.read(device, datastreamType, dataType, slaveAddress, dataAddress, readFromCache,
                        numRegistersToRead));
    }
}
