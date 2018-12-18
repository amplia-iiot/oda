package es.amplia.oda.core.commons.modbus;


public interface ModbusMaster {
    void connect() throws ModbusException;
    boolean readInputDiscrete(int unitId, int ref) throws ModbusException;
    Boolean[] readInputDiscretes(int unitId, int ref, int count) throws ModbusException;
    boolean readCoil(int unitId, int ref) throws ModbusException;
    Boolean[] readCoils(int unitId, int ref, int count) throws ModbusException;
    void writeCoil(int unitId, int ref, boolean value) throws ModbusException;
    void writeCoils(int unitId, int ref, Boolean[] values) throws ModbusException;
    Register readInputRegister(int unitId, int ref) throws ModbusException;
    Register[] readInputRegisters(int unitId, int ref, int count) throws ModbusException;
    Register readHoldingRegister(int unitId, int ref) throws ModbusException;
    Register[] readHoldingRegisters(int unitId, int ref, int count) throws ModbusException;
    void writeHoldingRegister(int unitId, int ref, Register register) throws ModbusException;
    void writeHoldingRegisters(int unitId, int ref, Register[] registers) throws ModbusException;
    void disconnect() throws ModbusException;
}
