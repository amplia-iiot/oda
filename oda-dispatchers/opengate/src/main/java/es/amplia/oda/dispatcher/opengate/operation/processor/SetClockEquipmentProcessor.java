package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationSetClock;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSetClock.*;

class SetClockEquipmentProcessor extends OperationProcessorTemplate<Long, Result> {

    static final String SET_CLOCK_EQUIPMENT_OPERATION_NAME = "SET_CLOCK_EQUIPMENT";


    private final OperationSetClock operationSetClock;


    SetClockEquipmentProcessor(Serializer serializer, OperationSetClock operationSetClock) {
        super(serializer);
        this.operationSetClock = operationSetClock;
    }

    @Override
    Long parseParameters(Request request) {
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        int offset = 0;

        if (request.getParameters() != null) {
            Map<String, ValueObject> params = request.getParameters().stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getName() != null)
                    .filter(p -> p.getValue() != null)
                    .collect(Collectors.toMap(Parameter::getName, Parameter::getValue));

            date = getParamAsString("date", params).map(this::parseLocalDate).orElse(date);
            time = getParamAsString("time", params).map(this::parseLocalTime).orElse(time);
            offset = getParamAsNumber("timezone", params).map(Number::intValue).orElse(0);
            offset += getParamAsNumber("dst", params).map(Number::intValue).orElse(0);
        }

        return ZonedDateTime.of(date, time, ZoneId.ofOffset("GMT", ZoneOffset.ofHours(offset))).toInstant()
                .toEpochMilli();
    }

    private Optional<String> getParamAsString(String name, Map<String, ValueObject> params) {
        ValueObject object = params.get(name);
        if (object != null) {
            String paramAsString = object.getString();
            if (paramAsString == null) {
                throw new IllegalArgumentException("Parameter \"" + name + "\" of incorrect type");
            }

            return Optional.of(paramAsString);
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

    private Optional<Double> getParamAsNumber(String name, Map<String, ValueObject> params) {
        ValueObject object = params.get(name);
        if (object != null) {
            Double paramAsNumber = object.getNumber();
            if (paramAsNumber == null) {
                throw new IllegalArgumentException("Parameter \"" + name + "\" of incorrect type");
            }

            return Optional.of(paramAsNumber);
        }

        return Optional.empty();
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, Long params) {
        return operationSetClock.setClock(deviceIdForOperations, params);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId) {
        Step setClockStep = new Step(SET_CLOCK_EQUIPMENT_OPERATION_NAME, getStepResult(result),
                result.getResultDescription(), null, null);
        Response response = new Response(requestId, deviceId, SET_CLOCK_EQUIPMENT_OPERATION_NAME,
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
