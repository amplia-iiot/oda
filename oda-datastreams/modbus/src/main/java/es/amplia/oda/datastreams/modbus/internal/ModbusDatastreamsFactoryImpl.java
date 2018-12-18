package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.datastreams.modbus.ModbusDatastreamsFactory;
import es.amplia.oda.datastreams.modbus.ModbusType;

import java.lang.reflect.Type;
import java.util.Map;

public class ModbusDatastreamsFactoryImpl implements ModbusDatastreamsFactory {

    private final ModbusReadOperatorProcessor readOperatorProcessor;
    private final ModbusWriteOperatorProcessor writeOperatorProcessor;


    public ModbusDatastreamsFactoryImpl(ModbusMaster modbusMaster) {
        this.readOperatorProcessor = new ModbusReadOperatorProcessor(modbusMaster, new ModbusTypeToJavaTypeConverter());
        this.writeOperatorProcessor = new ModbusWriteOperatorProcessor(modbusMaster, new JavaTypeToModbusTypeConverter());
    }

    @Override
    public ModbusDatastreamsGetter createModbusDatastreamsGetter(String datastreamId, Type datastreamType,
                                                                 Map<String, Integer> deviceIdSlaveAddressMapper,
                                                                 ModbusType dataType, int dataAddress) {
        return new ModbusDatastreamsGetter(datastreamId, datastreamType, deviceIdSlaveAddressMapper, dataType,
                dataAddress, readOperatorProcessor);
    }

    @Override
    public ModbusDatastreamsSetter createModbusDatastreamsSetter(String datastreamId, Type datastreamType,
                                                                 Map<String, Integer> deviceIdSlaveAddressMapper,
                                                                 ModbusType dataType, int dataAddress) {
        return new ModbusDatastreamsSetter(datastreamId, datastreamType, deviceIdSlaveAddressMapper, dataType,
                dataAddress, writeOperatorProcessor);
    }
}
