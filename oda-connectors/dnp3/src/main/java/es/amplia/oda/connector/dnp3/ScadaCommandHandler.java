package es.amplia.oda.connector.dnp3;

import com.automatak.dnp3.*;
import com.automatak.dnp3.enums.CommandStatus;
import com.automatak.dnp3.enums.OperateType;

import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.interfaces.ScadaDispatcher.ScadaOperation;
import static es.amplia.oda.core.commons.interfaces.ScadaDispatcher.ScadaOperationResult;

class ScadaCommandHandler implements CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScadaCommandHandler.class);

    private final ScadaDispatcher scadaDispatcher;


    ScadaCommandHandler(ScadaDispatcher scadaDispatcher) {
        this.scadaDispatcher = scadaDispatcher;
    }

    private <T> CommandStatus process(ScadaOperation operation, int index, T value) {
        ScadaOperationResult result;
        try {
            result = scadaDispatcher.process(operation, index, value, null).get();
        } catch (ExecutionException e) {
            LOGGER.error("Error processing SCADA operation {}", operation, e);
            result = ScadaOperationResult.ERROR;
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted exception processing SCADA operation {}", operation, e);
            result = ScadaOperationResult.ERROR;
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Operation {} on index {} with value {}: {}", operation, index, value, result);
        return toCommandStatus(result);
    }

    private CommandStatus toCommandStatus(ScadaOperationResult operationResult) {
        switch (operationResult) {
            case SUCCESS:
                return CommandStatus.SUCCESS;
            case ERROR:
                return CommandStatus.FORMAT_ERROR;
            case NOT_SUPPORTED:
                return CommandStatus.NOT_SUPPORTED;
            default:
                return CommandStatus.UNDEFINED;
        }
    }

    @Override
    public void start() {
        // Nothing to do at the start
    }

    @Override
    public CommandStatus selectCROB(ControlRelayOutputBlock command, int index) {
        return process(ScadaOperation.SELECT, index, command);
    }

    @Override
    public CommandStatus selectAOI32(AnalogOutputInt32 command, int index) {
        return process(ScadaOperation.SELECT, index, command.value);
    }

    @Override
    public CommandStatus selectAOI16(AnalogOutputInt16 command, int index) {
        return process(ScadaOperation.SELECT, index, command.value);
    }

    @Override
    public CommandStatus selectAOF32(AnalogOutputFloat32 command, int index) {
        return process(ScadaOperation.SELECT, index, command.value);
    }

    @Override
    public CommandStatus selectAOD64(AnalogOutputDouble64 command, int index) {
        return process(ScadaOperation.SELECT, index, command.value);
    }

    @Override
    public CommandStatus operateCROB(ControlRelayOutputBlock command, int index, OperateType opType) {
        return process(toScadaOperation(opType), index, command);
    }

    @Override
    public CommandStatus operateAOI32(AnalogOutputInt32 command, int index, OperateType opType) {
        return process(toScadaOperation(opType), index, command.value);
    }

    @Override
    public CommandStatus operateAOI16(AnalogOutputInt16 command, int index, OperateType opType) {
        return process(toScadaOperation(opType), index, command.value);
    }

    @Override
    public CommandStatus operateAOF32(AnalogOutputFloat32 command, int index, OperateType opType) {
        return process(toScadaOperation(opType), index, command.value);
    }

    @Override
    public CommandStatus operateAOD64(AnalogOutputDouble64 command, int index, OperateType opType) {
        return process(toScadaOperation(opType), index, command.value);
    }

    @Override
    public void end() {
        // Nothing to do at the end
    }

    private ScadaOperation toScadaOperation(OperateType opType) {
        switch(opType) {
            case SelectBeforeOperate:
                return ScadaOperation.SELECT_BEFORE_OPERATE;
            case DirectOperateNoAck:
                return ScadaOperation.DIRECT_OPERATE_NO_ACK;
            default:
                return ScadaOperation.DIRECT_OPERATE;
        }
    }
}
