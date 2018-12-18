package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.modbus.ModbusType;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

class ModbusDatastreamsGetter implements DatastreamsGetter {

    private final String datastreamId;
    private final Type datastreamType;
    private final Map<String, Integer> deviceIdSlaveAddressMapper;
    private final ModbusType dataType;
    private final int dataAddress;
    private final ModbusReadOperatorProcessor readOperatorProcessor;


    ModbusDatastreamsGetter(String datastreamId, Type datastreamType, Map<String, Integer> deviceIdSlaveAddressMapper,
                            ModbusType dataType, int dataAddress, ModbusReadOperatorProcessor readOperatorProcessor) {
        this.datastreamId = datastreamId;
        this.datastreamType = datastreamType;
        this.deviceIdSlaveAddressMapper = deviceIdSlaveAddressMapper;
        this.dataType = dataType;
        this.dataAddress = dataAddress;
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
        return CompletableFuture.supplyAsync(() ->
                readOperatorProcessor.read(datastreamType, dataType, slaveAddress, dataAddress));
    }
}
