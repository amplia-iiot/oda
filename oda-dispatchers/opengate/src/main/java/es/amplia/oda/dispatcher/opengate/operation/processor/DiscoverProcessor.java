package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.operation.api.OperationDiscover;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

public class DiscoverProcessor extends OperationProcessorTemplate<Void, OperationDiscover.Result> {

	public static final String DISCOVER_OPERATION_NAME = "DISCOVER";


	private final OperationDiscover operationDiscover;


	DiscoverProcessor(OperationDiscover operationRefreshInfo) {
		this.operationDiscover = operationRefreshInfo;
	}

	@Override
	Void parseParameters(Request request) {
		return null;
	}

	@Override
	CompletableFuture<OperationDiscover.Result> processOperation(String deviceIdForOperations, String operationId, Void params) {
		return operationDiscover.discover();
	}

	@Override
	Output translateToOutput(OperationDiscover.Result result, String requestId, String deviceId, String[] path) {
		List<Step> steps =
				Collections.singletonList(new Step(DISCOVER_OPERATION_NAME, StepResultCode.SUCCESSFUL, "", null, null));
		OutputOperation operation =
				new OutputOperation(new Response(requestId, deviceId, path, DISCOVER_OPERATION_NAME,
						OperationResultCode.SUCCESSFUL, "No Error.", steps));
		return new Output(OPENGATE_VERSION, operation);
	}
}
