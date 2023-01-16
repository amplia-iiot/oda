package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsManager;
import es.amplia.oda.datastreams.modbus.ModbusDatastreamsFactory;
import es.amplia.oda.datastreams.modbus.ModbusType;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ModbusDatastreamsFactoryImpl implements ModbusDatastreamsFactory {


    private final ModbusConnectionsManager modbusConnectionsManager;
    private final ModbusTypeToJavaTypeConverter modbusJavaConverter = new ModbusTypeToJavaTypeConverter();
    private final JavaTypeToModbusTypeConverter javaModbusConverter = new JavaTypeToModbusTypeConverter();


    public ModbusDatastreamsFactoryImpl(ModbusConnectionsManager modbusConnections) {
        this.modbusConnectionsManager = modbusConnections;
    }

    @Override
    public ModbusDatastreamsGetter createModbusDatastreamsGetter(String datastreamId, Type datastreamType,
                                                                 Map<String, Integer> deviceIdSlaveAddressMapper,
                                                                 ModbusType dataType, int dataAddress) {

        // get modbus connection corresponding to the deviceId
        ModbusMaster modbusConnection = getModbusConnectionWithId(deviceIdSlaveAddressMapper);

        if (modbusConnection != null) {

            ModbusReadOperatorProcessor readOperatorProcessor =
                    new ModbusReadOperatorProcessor(modbusConnection, modbusJavaConverter);

            return new ModbusDatastreamsGetter(datastreamId, datastreamType, deviceIdSlaveAddressMapper, dataType,
                    dataAddress, readOperatorProcessor);
        }

        return null;
    }

    @Override
    public ModbusDatastreamsSetter createModbusDatastreamsSetter(String datastreamId, Type datastreamType,
                                                                 Map<String, Integer> deviceIdSlaveAddressMapper,
                                                                 ModbusType dataType, int dataAddress) {

        // get modbus connection corresponding to the deviceId
        ModbusMaster modbusConnection = getModbusConnectionWithId(deviceIdSlaveAddressMapper);

        if (modbusConnection != null) {

            ModbusWriteOperatorProcessor writeOperatorProcessor =
                    new ModbusWriteOperatorProcessor(modbusConnection, javaModbusConverter);

            return new ModbusDatastreamsSetter(datastreamId, datastreamType, deviceIdSlaveAddressMapper, dataType,
                    dataAddress, writeOperatorProcessor);
        }

        return null;
    }

    private ModbusMaster getModbusConnectionWithId(Map<String, Integer> deviceIdSlaveAddressMapper) {

        // retrieve deviceId from map
        Stream<String> deviceIds = deviceIdSlaveAddressMapper.keySet().stream();
        Optional<String> deviceId = deviceIds.findFirst();

        // retrieve connection corresponding to the deviceId
        return deviceId.map(modbusConnectionsManager::getModbusConnectionWithId).orElse(null);
    }
}
