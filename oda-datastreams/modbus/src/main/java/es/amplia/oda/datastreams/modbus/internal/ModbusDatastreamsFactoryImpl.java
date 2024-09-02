package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.datastreams.modbus.ModbusDatastreamsFactory;
import es.amplia.oda.datastreams.modbus.ModbusType;

import java.lang.reflect.Type;
import java.util.Map;

public class ModbusDatastreamsFactoryImpl implements ModbusDatastreamsFactory {

    private final ModbusReadOperatorProcessor readOperatorProcessor;
    private final ModbusWriteOperatorProcessor writeOperatorProcessor;


    public ModbusDatastreamsFactoryImpl(ModbusConnectionsFinder modbusConnectionsFinder) {
        this.readOperatorProcessor = new ModbusReadOperatorProcessor(modbusConnectionsFinder,
                new ModbusTypeToJavaTypeConverter());
        this.writeOperatorProcessor = new ModbusWriteOperatorProcessor(modbusConnectionsFinder,
                new JavaTypeToModbusTypeConverter());
    }

    @Override
    public ModbusDatastreamsGetter createModbusDatastreamsGetter(String datastreamId, Type datastreamType,
                                                                 Map<String, Integer> deviceIdSlaveAddressMapper,
                                                                 ModbusType dataType, int dataAddress,
                                                                 boolean readFromCache, int numRegistersToRead) {
        return new ModbusDatastreamsGetter(datastreamId, datastreamType, deviceIdSlaveAddressMapper, dataType,
                dataAddress, readFromCache, numRegistersToRead, readOperatorProcessor);
    }

    @Override
    public ModbusDatastreamsSetter createModbusDatastreamsSetter(String datastreamId, Type datastreamType,
                                                                 Map<String, Integer> deviceIdSlaveAddressMapper,
                                                                 ModbusType dataType, int dataAddress) {
        return new ModbusDatastreamsSetter(datastreamId, datastreamType, deviceIdSlaveAddressMapper, dataType,
                dataAddress, writeOperatorProcessor);
    }
}
