package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationRefreshInfo.*;

class RefreshInfoProcessor extends OperationProcessorTemplate<Void, Result> {

    static final String REFRESH_INFO_OPERATION_NAME = "REFRESH_INFO";


    private final OperationRefreshInfo operationRefreshInfo;


    RefreshInfoProcessor(OperationRefreshInfo operationRefreshInfo) {
        this.operationRefreshInfo = operationRefreshInfo;
    }

    @Override
    Void parseParameters(Request request) {
        // Operation with no params
        return null;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, Void params) {
        return operationRefreshInfo.refreshInfo(deviceIdForOperations);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        List<Object> outputVariables = result.getValues().entrySet().stream()
                .map(es -> new OutputVariable(es.getKey(), es.getValue(), SUCCESS_RESULT, SUCCESS_RESULT))
                .collect(Collectors.toList());
        List<Step> steps =
                Collections.singletonList(new Step(REFRESH_INFO_OPERATION_NAME, StepResultCode.SUCCESSFUL, "",
                        null, outputVariables));
        OutputOperation operation =
                new OutputOperation(new Response(requestId, deviceId, path, REFRESH_INFO_OPERATION_NAME,
                        OperationResultCode.SUCCESSFUL, "No Error.", steps));
        return new Output(OPENGATE_VERSION, operation);
    }
}
