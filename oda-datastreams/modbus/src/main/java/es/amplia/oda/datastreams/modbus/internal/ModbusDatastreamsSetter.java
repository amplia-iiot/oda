package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.datastreams.modbus.ModbusType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class ModbusDatastreamsSetter implements DatastreamsSetter {

    private final String datastreamId;
    private final Type datastreamType;
    private final Map<String, Integer> devicesIdSlaveAddressMapper;
    private final ModbusType dataType;
    private final int dataAddress;
    private final ModbusWriteOperatorProcessor writeOperatorProcessor;


    ModbusDatastreamsSetter(String datastreamId, Type datastreamType, Map<String, Integer> devicesIdSlaveAddressMapper,
                            ModbusType dataType, int dataAddress, ModbusWriteOperatorProcessor writeOperatorProcessor) {
        this.datastreamId = datastreamId;
        this.datastreamType = datastreamType;
        this.devicesIdSlaveAddressMapper = devicesIdSlaveAddressMapper;
        this.dataType = dataType;
        this.dataAddress = dataAddress;
        this.writeOperatorProcessor = writeOperatorProcessor;
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public Type getDatastreamType() {
        return datastreamType;
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return new ArrayList<>(devicesIdSlaveAddressMapper.keySet());
    }

    @Override
    public CompletableFuture<Void> set(String device, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            int slaveAddress = Optional.ofNullable(devicesIdSlaveAddressMapper.get(device))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown device identifier"));
            writeOperatorProcessor.write(datastreamType, dataType, slaveAddress, dataAddress, value);
            return null;
        });
    }
}
