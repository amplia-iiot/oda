package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationCreateRule;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

public class CreateRuleProcessor extends OperationProcessorTemplate<Map<String, String>, OperationCreateRule.Result> {

	static final String CREATE_RULE_OPERATION_NAME = "CREATE_RULE";

	private final OperationCreateRule operationCreateRule;

	public CreateRuleProcessor(OperationCreateRule operationCreateRule) {
		this.operationCreateRule = operationCreateRule;
	}

	@Override
	Map<String, String> parseParameters(Request request) {
		if (request.getParameters() == null) {
			throw new IllegalArgumentException("No parameters in " + CREATE_RULE_OPERATION_NAME);
		}

		Map<String, ValueObject> params = request.getParameters().stream()
				.filter(Objects::nonNull)
				.filter(p -> p.getName() != null)
				.filter(p -> p.getValue() != null)
				.collect(Collectors.toMap(Parameter::getName, Parameter::getValue));

		if(params.size() != 4) {
			throw new IllegalArgumentException("Expected four parameters in " + CREATE_RULE_OPERATION_NAME);
		}

		ValueObject datastreamId = params.get("datastreamId");
		ValueObject namerule = params.get("namerule");
		ValueObject when = params.get("when");
		ValueObject then = params.get("then");
		if (null == datastreamId) {
			throw new IllegalArgumentException("Parameter datastreamId not found");
		}
		if (null == namerule) {
			throw new IllegalArgumentException("Parameter namerule not found");
		}
		if (null == when) {
			throw new IllegalArgumentException("Parameter when not found");
		}
		if (null == then) {
			throw new IllegalArgumentException("Parameter then not found");
		}
		if (null == datastreamId.getString()) {
			throw new IllegalArgumentException("Parameter datastreamId of incorrect type");
		}
		if (null == namerule.getString()) {
			throw new IllegalArgumentException("Parameter namerule of incorrect type");
		}
		if (null == when.getString()) {
			throw new IllegalArgumentException("Parameter when of incorrect type");
		}
		if (null == then.getString()) {
			throw new IllegalArgumentException("Parameter then of incorrect type");
		}

		Map<String, String> data = new HashMap<>();
		data.put("datastreamId", datastreamId.getString());
		data.put("namerule", namerule.getString());
		data.put("when", when.getString());
		data.put("then", then.getString());

		return data;
	}

	@Override
	CompletableFuture<OperationCreateRule.Result> processOperation(String deviceIdForOperations, Map<String, String> params) {
		return operationCreateRule.createRule(deviceIdForOperations, params);
	}

	@Override
	Output translateToOutput(OperationCreateRule.Result result, String requestId, String deviceId, String[] path) {
		Step createRuleStep = new Step(CREATE_RULE_OPERATION_NAME, getStepResult(result),
				result.getResultDescription(), null, null);
		Response response = new Response(requestId, deviceId, path, CREATE_RULE_OPERATION_NAME,
				getOperationCode(result), result.getResultDescription(), Collections.singletonList(createRuleStep));
		OutputOperation operation = new OutputOperation(response);
		return new Output(OPENGATE_VERSION, operation);
	}

	private StepResultCode getStepResult(OperationCreateRule.Result result) {
		return OperationCreateRule.Status.OK.equals(result.getResultCode())?
				StepResultCode.SUCCESSFUL: StepResultCode.ERROR;
	}

	private OperationResultCode getOperationCode(OperationCreateRule.Result result) {
		return OperationCreateRule.Status.OK.equals(result.getResultCode())?
				OperationResultCode.SUCCESSFUL: OperationResultCode.ERROR_PROCESSING;
	}
}
