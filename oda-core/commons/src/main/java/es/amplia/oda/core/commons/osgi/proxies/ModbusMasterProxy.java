package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.modbus.ModbusException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;

import org.osgi.framework.BundleContext;

import java.util.Optional;

public class ModbusMasterProxy implements ModbusMaster, AutoCloseable {

    static <T> Consumer<T> throwingModbusExceptionWrapper(Consumer<T> consumer) {
        return i -> {
            try {
                consumer.accept(i);
            } catch (ModbusException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    private static final String NO_MODBUS_MASTER_AVAILABLE_MESSAGE = "No Modbus Master available";

    private final OsgiServiceProxy<ModbusMaster> proxy;

    public ModbusMasterProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(ModbusMaster.class, bundleContext);
    }

    @Override
    public void connect() {
        proxy.consumeFirst(ModbusMaster::connect);
    }

    @Override
    public boolean readInputDiscrete(int unitId, int ref) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readInputDiscrete(unitId, ref)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public Boolean[] readInputDiscretes(int unitId, int ref, int count) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readInputDiscretes(unitId, ref, count)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));

    }

    @Override
    public boolean readCoil(int unitId, int ref) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readCoil(unitId, ref)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public Boolean[] readCoils(int unitId, int ref, int count) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readCoils(unitId, ref, count)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public void writeCoil(int unitId, int ref, boolean value) {
        proxy.consumeFirst(modbusMaster -> modbusMaster.writeCoil(unitId, ref, value));
    }

    @Override
    public void writeCoils(int unitId, int ref, Boolean[] values) {
        proxy.consumeFirst(modbusMaster -> modbusMaster.writeCoils(unitId, ref, values));
    }

    @Override
    public Register readInputRegister(int unitId, int ref) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readInputRegister(unitId, ref)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public Register[] readInputRegisters(int unitId, int ref, int count) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readInputRegisters(unitId, ref, count)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public Register readHoldingRegister(int unitId, int ref) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readHoldingRegister(unitId, ref)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public Register[] readHoldingRegisters(int unitId, int ref, int count) {
        return Optional.ofNullable(proxy.callFirst(modbusMaster -> modbusMaster.readHoldingRegisters(unitId, ref, count)))
                .orElseThrow(() -> new ModbusException(NO_MODBUS_MASTER_AVAILABLE_MESSAGE));
    }

    @Override
    public void writeHoldingRegister(int unitId, int ref, Register register) {
        proxy.consumeFirst(modbusMaster -> modbusMaster.writeHoldingRegister(unitId, ref, register));
    }

    @Override
    public void writeHoldingRegisters(int unitId, int ref, Register[] registers) {
        proxy.consumeFirst(modbusMaster -> modbusMaster.writeHoldingRegisters(unitId, ref, registers));
    }

    @Override
    public void disconnect() {
        proxy.consumeFirst(ModbusMaster::disconnect);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
