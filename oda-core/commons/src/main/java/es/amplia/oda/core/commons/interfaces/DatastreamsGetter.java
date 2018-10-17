package es.amplia.oda.core.commons.interfaces;

import lombok.Value;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface registered in OSGi and used by the rest of bundles to get the value of a single datastream (aka "variables").<p>
 * A DatastreamsGetter can manage more than one device. The function {@link #getDevicesIdManaged() getDevicesIdManaged} is
 * used to know the list of devices currently handled by a DatastreamGetter.
 */
public interface DatastreamsGetter {
	
	@Value
	class CollectedValue {
		private final long at;
		private final Object value;
	}
	
	/**
	 * 
	 * @return The Datastream identifier that this DatastreamsGetter generates with every invocation to get().
	 */
	String getDatastreamIdSatisfied();
	
	/**
	 * 
	 * @return The list of devices currently managed by this DatastreamGetter. The empty string ("") represents ODA itself,
	 * so in the common case of a datastream that only manages ODA itself, this function should return {@code Arrays.asList("")}.   
	 */
	List<String> getDevicesIdManaged();
	
	/**
	 * This functions should start an asynchronous operation that, when completed, will obtain a new value for the
	 * datastream managed by this DatastreamsGetter, but only for the device specified in the parameter.<p>
	 * Of course, it is permissible to return immediately a value, that is, a CompletableFuture already completed, but
	 * it is not permissible to block until the value is obtained.
	 * @param device Device to get the datastream from.
	 * @return A CompletableFuture that will be used to concatenate code to be executed when the asynchronous operation finishes.  
	 */
	CompletableFuture<CollectedValue> get(String device);
}
