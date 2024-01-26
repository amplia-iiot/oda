package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.dispatcher.opengate.domain.setclock.Datetime;
import es.amplia.oda.dispatcher.opengate.domain.setclock.ParameterSetClockOperation;
import es.amplia.oda.dispatcher.opengate.domain.setclock.RequestSetClockOperation;
import es.amplia.oda.operation.api.OperationSetClock;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSetClock.*;

public class SetClockEquipmentProcessor extends OperationProcessorTemplate<Long, Result> {

    public static final String SET_CLOCK_EQUIPMENT_OPERATION_NAME = "SET_CLOCK_EQUIPMENT";


    private final OperationSetClock operationSetClock;


    SetClockEquipmentProcessor(OperationSetClock operationSetClock) {
        this.operationSetClock = operationSetClock;
    }

    @Override
    Long parseParameters(Request request) {
        RequestSetClockOperation specificRequest = (RequestSetClockOperation) request;
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        int offset = 0;

        ParameterSetClockOperation parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }

        if(parameters == null) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }

        Datetime datetime = parameters.getDatetime();

        if(datetime != null && datetimeNotEmpty(datetime)) {
            date = parseLocalDate(datetime.getDate());
            time = parseLocalTime(datetime.getTime());
            offset = datetime.getTimezone();
            offset += datetime.getDst();
        }

        return ZonedDateTime.of(date, time, ZoneId.ofOffset("GMT", ZoneOffset.ofHours(offset))).toInstant()
                .toEpochMilli();
    }

    private boolean datetimeNotEmpty(Datetime datetime) {
        return datetime.getDate() != null && datetime.getTime() != null;
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
