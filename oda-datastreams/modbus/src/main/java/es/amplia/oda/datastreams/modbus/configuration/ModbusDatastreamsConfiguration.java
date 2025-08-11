package es.amplia.oda.datastreams.modbus.configuration;

import es.amplia.oda.hardware.modbus.ModbusType;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.lang.reflect.Type;
import java.util.List;

@Value
@Builder(builderClassName = "ModbusDatastreamsConfigurationBuilder")
public class ModbusDatastreamsConfiguration {
    @NonNull
    String datastreamId;
    @NonNull
    String deviceId;
    @NonNull
    Type datastreamType;
    int slaveAddress;
    @NonNull
    ModbusType dataType;
    int dataAddress;
    @Builder.Default
    int numRegistersToRead = 1;
    @Builder.Default
    boolean readFromCache = false;

    public static class ModbusDatastreamsConfigurationBuilder {
        private String datastreamId;
        private String deviceId;
        private Type datastreamType;
        private int slaveAddress;
        private ModbusType dataType;
        private int dataAddress;
        private int numRegistersToRead;
        private boolean readFromCache;

        public ModbusDatastreamsConfigurationBuilder datastreamId(String datastreamId) {
            this.datastreamId = datastreamId;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder datastreamType(Type datastreamType) {
            this.datastreamType = datastreamType;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder slaveAddress(int slaveAddress) {
            this.slaveAddress = slaveAddress;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder dataType(ModbusType dataType) {
            this.dataType = dataType;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder dataAddress(int dataAddress) {
            this.dataAddress = dataAddress;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder readFromCache(boolean readFromCache) {
            this.readFromCache = readFromCache;
            return this;
        }

        public ModbusDatastreamsConfigurationBuilder numRegistersToRead(int numRegistersToRead) {
            this.numRegistersToRead = numRegistersToRead;
            return this;
        }

        public ModbusDatastreamsConfiguration build() {
            validateTypesCompatible();

            return new ModbusDatastreamsConfiguration(datastreamId, deviceId, datastreamType, slaveAddress, dataType,
                    dataAddress, numRegistersToRead, readFromCache);
        }

        private void validateTypesCompatible() {
            if ((isBitDataType() && !isBitDataTypeCompatible()) || (is16BitDataType() && !is16BitDataTypeCompatible())) {
                throw new IllegalArgumentException("Incompatible data type " + dataType + " and datastream data type "
                        + datastreamType);
            }
        }

        private boolean isBitDataType() {
            return ModbusType.INPUT_DISCRETE.equals(dataType) || ModbusType.COIL.equals(dataType);
        }

        private boolean isBitDataTypeCompatible() {
            return Boolean.class.equals(datastreamType) || listBitCompatible();
        }

        private boolean listBitCompatible() {
            return List.class.equals(datastreamType);
        }


        private boolean is16BitDataType() {
            return ModbusType.INPUT_REGISTER.equals(dataType) || ModbusType.HOLDING_REGISTER.equals(dataType);
        }

        private boolean is16BitDataTypeCompatible() {
            return one16BitRegisterCompatible() || two16BitRegisterCompatible() || four16BitCompatible() || list16BitCompatible();
        }

        private boolean one16BitRegisterCompatible() {
            return Byte[].class.equals(datastreamType) || Short.class.equals(datastreamType);
        }

        private boolean two16BitRegisterCompatible() {
            return Integer.class.equals(datastreamType) || Float.class.equals(datastreamType);
        }

        private boolean four16BitCompatible() {
            return Long.class.equals(datastreamType) || Double.class.equals(datastreamType);
        }

        private boolean list16BitCompatible() {
            return List.class.equals(datastreamType);
        }
    }
}
