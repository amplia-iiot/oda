package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationDiscover;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class DiscoverProcessor extends OperationProcessorTemplate<Void, OperationDiscover.Result> {

	static final String DISCOVER_OPERATION_NAME = "DISCOVER";


	private final OperationDiscover operationDiscover;


	DiscoverProcessor(Serializer serializer, OperationDiscover operationRefreshInfo) {
		super(serializer);
		this.operationDiscover = operationRefreshInfo;
	}

	@Override
	Void parseParameters(Request request) {
		return null;
	}

	@Override
	CompletableFuture<OperationDiscover.Result> processOperation(String deviceIdForOperations, Void params) {
		return operationDiscover.discover();
	}

	@Override
	Output translateToOutput(OperationDiscover.Result result, String requestId, String deviceId) {
		List<Step> steps =
				Collections.singletonList(new Step(DISCOVER_OPERATION_NAME, StepResultCode.SUCCESSFUL, "", 0L, null));
		OutputOperation operation =
				new OutputOperation(new Response(requestId, deviceId, DISCOVER_OPERATION_NAME,
						OperationResultCode.SUCCESSFUL, "No Error.", steps));
		return new Output(OPENGATE_VERSION, operation);
	}
}
