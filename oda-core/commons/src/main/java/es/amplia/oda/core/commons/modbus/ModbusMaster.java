package es.amplia.oda.core.commons.modbus;


public interface ModbusMaster {

    String getDeviceId();
    void connect();
    boolean readInputDiscrete(int unitId, int ref);
    Boolean[] readInputDiscretes(int unitId, int ref, int count);
    boolean readCoil(int unitId, int ref);
    Boolean[] readCoils(int unitId, int ref, int count);
    void writeCoil(int unitId, int ref, boolean value);
    void writeCoils(int unitId, int ref, Boolean[] values);
    Register readInputRegister(int unitId, int ref);
    Register[] readInputRegisters(int unitId, int ref, int count);
    Register readHoldingRegister(int unitId, int ref);
    Register[] readHoldingRegisters(int unitId, int ref, int count);
    void writeHoldingRegister(int unitId, int ref, Register register);
    void writeHoldingRegisters(int unitId, int ref, Register[] registers);
    void disconnect();
}
