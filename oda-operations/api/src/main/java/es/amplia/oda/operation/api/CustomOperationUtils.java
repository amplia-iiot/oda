package es.amplia.oda.operation.api;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.core.commons.utils.operation.response.StepResultCode;
import es.amplia.oda.operation.api.CustomOperation.Result;
import es.amplia.oda.operation.api.CustomOperation.Status;
import es.amplia.oda.operation.api.CustomOperation.Step;
import es.amplia.oda.operation.api.CustomOperation.StepStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomOperationUtils {

    public static Result translateToResult (Response response) {
        if (response == null) return null;
        List<Step> steps = translateSteps(response.getSteps());
        return new Result(getOperationResult(response.getResultCode()), response.getResultDescription(), steps);
    }

    private static Status getOperationResult(OperationResultCode resultCode) {
        switch (resultCode) {
            case SUCCESSFUL: return Status.SUCCESSFUL;
            case ERROR_IN_PARAM: return Status.ERROR_IN_PARAM;
            default: return Status.ERROR_PROCESSING;
        }
    }

    private static List<Step> translateSteps(List<es.amplia.oda.core.commons.utils.operation.response.Step> list) {
        if ( (list == null) || (list.isEmpty()) ) return Collections.emptyList();
        return list.stream().map(CustomOperationUtils::translateStep).collect(Collectors.toList());
    }

    private static Step translateStep(es.amplia.oda.core.commons.utils.operation.response.Step step) {
        log.debug("Step to translate: " + step);
        return new Step(step.getName(), getStepResult(step.getResult()), step.getDescription(), step.getTimestamp()==null?System.currentTimeMillis():step.getTimestamp(), step.getResponse());
    }

    private static StepStatus getStepResult(StepResultCode result) {
        switch (result) {
            case ERROR: return StepStatus.ERROR;
            case SKIPPED: return StepStatus.SKIPPED;
            default: return StepStatus.SUCCESSFUL;
        }
    }

}
