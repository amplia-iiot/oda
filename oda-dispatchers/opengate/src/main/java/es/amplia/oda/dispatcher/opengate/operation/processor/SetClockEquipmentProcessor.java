package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ParameterSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ValueSetting;
import es.amplia.oda.operation.api.OperationSetClock;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSetClock.*;

class SetClockEquipmentProcessor extends OperationProcessorTemplate<Long, Result> {

    static final String SET_CLOCK_EQUIPMENT_OPERATION_NAME = "SET_CLOCK_EQUIPMENT";


    private final OperationSetClock operationSetClock;


    SetClockEquipmentProcessor(OperationSetClock operationSetClock) {
        this.operationSetClock = operationSetClock;
    }

    @Override
    Long parseParameters(Request request) {
        RequestSetOrConfigureOperation specificRequest = (RequestSetOrConfigureOperation) request;
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        int offset = 0;

        ParameterSetOrConfigureOperation parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }

        if(parameters == null) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }
        List<ValueSetting> params = parameters.getVariableList();

        date = getParamAsString("date", params).map(this::parseLocalDate).orElse(date);
        time = getParamAsString("time", params).map(this::parseLocalTime).orElse(time);
        offset = getParamAsNumber("timezone", params).map(Number::intValue).orElse(0);
        offset += getParamAsNumber("dst", params).map(Number::intValue).orElse(0);

        return ZonedDateTime.of(date, time, ZoneId.ofOffset("GMT", ZoneOffset.ofHours(offset))).toInstant()
                .toEpochMilli();
    }

    private Optional<String> getParamAsString(String name, List<ValueSetting> params) {
        String value = null;
        for (ValueSetting setting: params) {
            if (setting.getName().equals(name)) {
                value = setting.getValue();
            }
        }
        if (value != null) {
            return Optional.of(value);
        }

        return Optional.empty();
    }

    private LocalDate parseLocalDate(String dateAsString) {
        try {
            return LocalDate.parse(dateAsString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date \"" + dateAsString + "\" has invalid format");
        }
    }

    private LocalTime parseLocalTime(String timeAsString) {
        try {
            return LocalTime.parse(timeAsString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Time \"" + timeAsString+ "\" has invalid format");
        }
    }

    private Optional<Double> getParamAsNumber(String name, List<ValueSetting> params) {
        Double value = null;
        for (ValueSetting setting: params) {
            if (setting.getName().equals(name)) {
                value = Double.valueOf(setting.getValue());
            }
        }
        if (value != null) {
            return Optional.of(value);
        }

        return Optional.empty();
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, Long params) {
        return operationSetClock.setClock(deviceIdForOperations, params);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        Step setClockStep = new Step(SET_CLOCK_EQUIPMENT_OPERATION_NAME, getStepResult(result),
                result.getResultDescription(), null, null);
        Response response = new Response(requestId, deviceId, path, SET_CLOCK_EQUIPMENT_OPERATION_NAME,
                getOperationResult(result), result.getResultDescription(), Collections.singletonList(setClockStep));
        OutputOperation operation = new OutputOperation(response);
        return new Output(OPENGATE_VERSION, operation);
    }

    private StepResultCode getStepResult(Result result) {
        return ResultCode.SUCCESSFUL.equals(result.getResultCode())? StepResultCode.SUCCESSFUL: StepResultCode.ERROR;
    }

    private OperationResultCode getOperationResult(Result result) {
        return ResultCode.SUCCESSFUL.equals(result.getResultCode())?
                OperationResultCode.SUCCESSFUL: OperationResultCode.ERROR_PROCESSING;
    }
}
