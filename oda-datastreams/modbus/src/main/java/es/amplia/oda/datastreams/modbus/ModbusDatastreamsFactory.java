package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.hardware.modbus.ModbusType;

import java.lang.reflect.Type;
import java.util.Map;

public interface ModbusDatastreamsFactory {
    DatastreamsGetter createModbusDatastreamsGetter(String datastreamId, Type datastreamType,
                                                    Map<String, Integer> deviceIdSlaveAddressMapper,
                                                    ModbusType dataType, int dataAddress, boolean readFromCache,
                                                    int numRegistersToRead);

    DatastreamsSetter createModbusDatastreamsSetter(String datastreamId, Type datastreamType,
                                                    Map<String, Integer> deviceIdSlaveAddressMapper,
                                                    ModbusType dataType, int dataAddress);

    void updateDevicesCaches();
}
