package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OperationSetDeviceParameters {
	
	@Value
	class VariableValue {
		String identifier;
		Object value;
	}
	
	@Value
	class VariableResult {
		String identifier; //Not null
		String error; //null if the set operation finish without error
	}
	
	enum ResultCode {
		SUCCESSFUL,
		ERROR_IN_PARAM
	}

	@Value
	class Result {
		ResultCode resulCode;
		String resultDescription; //null if resulCode == SUCCESSFUL
		List<VariableResult> variables; //null if resultCode == ERROR_IN_PARAM
	}
	
	/**
	 * Search the system for datastreamsSetters and:<ul>
	 * <li> If one or more datastreamsSetters are not found, nothing is done and an ERROR_IN_PARAM is returned.</li>
	 * <li> If all datastreamsSetters are found, execute a set() operation in each of them, 
	 * in order. Whether or not each of the individual sets is successful, the next one will be always executed.</li>
	 * </ul>
	 * @param deviceId If an empty string (""), the datastreamsSetters must be for ODA itself, otherwise, 
	 * the datastreamsSetters must be for the device specified in this parameter. Not null.
	 * @param values The list of datastreamsSetters to look for and the value to set. Not null. 
	 * @return If any of the identifiers in param values is not found, a {@link Result} with
	 * {@literal resultCode == ERROR_IN_PARAM} and a sensible value in {@literal resultDescription};
	 * otherwise, a {@link Result} with {@literal resultCode == SUCCESSFUL} and a list of
	 * individual results for each variable.
	 */
	CompletableFuture<Result> setDeviceParameters(String deviceId, List<VariableValue> values);
}
