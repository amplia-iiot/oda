package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.modbus.configuration.ModbusDatastreamsConfiguration;

import lombok.Value;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModbusDatastreamsManager implements AutoCloseable {

    private final ModbusDatastreamsFactory modbusDatastreamsFactory;
    private final ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager;
    private final ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager;


    ModbusDatastreamsManager(ModbusDatastreamsFactory modbusDatastreamsFactory,
                             ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager,
                             ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager) {
        this.modbusDatastreamsFactory = modbusDatastreamsFactory;
        this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;
        this.datastreamsSetterRegistrationManager = datastreamsSetterRegistrationManager;
    }

    @Value
    private static class ModbusConfigurationKey {
        String datastreamId;
        Type datastreamType;
        ModbusType modbusType;
        int dataAddress;
        boolean readFromCache;
        int numRegistersToRead;
    }

    public void loadConfiguration(List<ModbusDatastreamsConfiguration> currentModbusDatastreamsConfigurations) {
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();

        Map<ModbusConfigurationKey, Map<String, Integer>> configurationByDatastreamId =
                currentModbusDatastreamsConfigurations.stream()
                        .collect(Collectors.groupingBy(this::createModbusConfigurationKey,
                                Collectors.toMap(ModbusDatastreamsConfiguration::getDeviceId,
                                        ModbusDatastreamsConfiguration::getSlaveAddress)));

        configurationByDatastreamId.forEach(this::createAndRegisterModbusDatastreams);
    }

    private ModbusConfigurationKey createModbusConfigurationKey(ModbusDatastreamsConfiguration conf) {
        return new ModbusConfigurationKey(conf.getDatastreamId(), conf.getDatastreamType(), conf.getDataType(),
                conf.getDataAddress(), conf.isReadFromCache(), conf.getNumRegistersToRead());
    }

    private void createAndRegisterModbusDatastreams(ModbusConfigurationKey modbusConfigurationKey,
                                                    Map<String, Integer> devicesIdModbusSlaveMapper) {
        if (isWriteableModbusType(modbusConfigurationKey)) {
            createAndRegisterModbusDatastreamsSetter(modbusConfigurationKey, devicesIdModbusSlaveMapper);
        }
        createAndRegisterModbusDatastreamsGetter(modbusConfigurationKey, devicesIdModbusSlaveMapper);
    }

    private boolean isWriteableModbusType(ModbusConfigurationKey modbusConfigurationKey) {
        return ModbusType.COIL.equals(modbusConfigurationKey.getModbusType()) ||
                ModbusType.HOLDING_REGISTER.equals(modbusConfigurationKey.getModbusType());

    }

    private void createAndRegisterModbusDatastreamsGetter(ModbusConfigurationKey modbusConfigurationKey,
                                                          Map<String, Integer> devicesIdModbusSlaveMapper) {
        DatastreamsGetter datastreamsGetter =
                modbusDatastreamsFactory.createModbusDatastreamsGetter(modbusConfigurationKey.datastreamId,
                        modbusConfigurationKey.datastreamType, devicesIdModbusSlaveMapper,
                        modbusConfigurationKey.getModbusType(), modbusConfigurationKey.getDataAddress(),
                        modbusConfigurationKey.readFromCache, modbusConfigurationKey.getNumRegistersToRead());

        if (datastreamsGetter != null) {
            datastreamsGetterRegistrationManager.register(datastreamsGetter);
        }
    }

    private void createAndRegisterModbusDatastreamsSetter(ModbusConfigurationKey modbusConfigurationKey,
                                                          Map<String, Integer> devicesIdModbusSlaveMapper) {
        DatastreamsSetter datastreamsSetter =
                modbusDatastreamsFactory.createModbusDatastreamsSetter(modbusConfigurationKey.datastreamId,
                        modbusConfigurationKey.datastreamType, devicesIdModbusSlaveMapper,
                        modbusConfigurationKey.getModbusType(), modbusConfigurationKey.getDataAddress());

        if (datastreamsSetter != null) {
            datastreamsSetterRegistrationManager.register(datastreamsSetter);
        }
    }

    @Override
    public void close() {
        datastreamsGetterRegistrationManager.unregister();
        datastreamsSetterRegistrationManager.unregister();
    }
}
