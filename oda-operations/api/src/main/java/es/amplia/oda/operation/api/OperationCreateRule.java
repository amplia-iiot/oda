package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface OperationCreateRule {
	enum Status {
		OK,
		ERROR_CREATING,
		ALREADY_EXISTS
	}

	@Value
	class Result {
		Status resultCode;
		String resultDescription;
	}

	CompletableFuture<Result> createRule(String deviceId, Map<String, String> ruleInfo);
}
