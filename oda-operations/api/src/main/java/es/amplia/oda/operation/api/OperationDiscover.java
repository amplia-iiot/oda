package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.concurrent.CompletableFuture;

public interface OperationDiscover {

	enum ResultCode {
		SUCCESSFUL,
		ERROR_PROCESSING,
	}

	@Value
	class Result {
		OperationDiscover.ResultCode resultCode;
		String resultDescription; // null if resultCode == SUCCESSFUL
	}

	CompletableFuture<Result> discover();
}
