package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.concurrent.CompletableFuture;

public interface OperationSetClock {

	enum ResultCode {
		SUCCESSFUL,
		ERROR_PROCESSING
	}

	@Value
	class Result {
		ResultCode resultCode;
		String resultDescription; // null if resultCode == SUCCESSFUL
	}

	CompletableFuture<Result> setClock(String deviceId, long timestamp);
}
