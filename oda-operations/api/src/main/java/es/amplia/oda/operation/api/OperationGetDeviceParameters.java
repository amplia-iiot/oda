package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface OperationGetDeviceParameters {
	
	public static enum Status {
		OK,
		NOT_FOUND,
		PROCESSING_ERROR
	}
	
	@Value
	public static class GetValue {
		String datastreamId;
		Status status;
		Object value; //null if status != OK
		String error; //null if status != PROCESSING_ERROR
	}
	
	@Value
	public static class Result {
		List<GetValue> values;
	}
	
	/**
	 * Search the system for datastreamsGetters and execute a get() operation in each of them.  
	 * @param deviceId If null, the datastreamsGetters must be for ODA itself, otherwise, 
	 * the datastreamsGetters must be for the device specified in this parameter.
	 * @param dataStreamIds The set of datastreamsGetters identifiers to look for. Not null. 
	 * @return A set of not found datastreamsGetters identifiers; and the result of the operation get() in
	 * each of the datastreamsGetters identifiers found.
	 */
	CompletableFuture<Result> getDeviceParameters(String deviceId, Set<String> dataStreamIds);
}
