package es.amplia.oda.core.commons.interfaces;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface registered in OSGi and used by the rest of bundles to set the value of a  datastream (aka "variable").
 * A DatastreamsSetter can manage more than one device. When a datastream is set, the device is passed as parameter.
 */
public interface DatastreamsSetter {
	
	/**
	 * 
	 * @return The Datastream identifier that this DatastreamsSetter handles.
	 */
	String getDatastreamIdSatisfied();
	
	/**
	 * 
	 * @return The Datastream type that this DatastreamsSetter needs to receive in the second parameter of {@link #set(String, Object)}.
	 */
	Type getDatastreamType();
	
	/**
	 * 
	 * @return The list of devices currently managed by this DatastreamSetter. The empty string ("") represents ODA itself,
	 * so in the common case of a datastream that only manages ODA itself, this function should return {@code Arrays.asList("")}.   
	 */
	List<String> getDevicesIdManaged();

	/**
	 * This functions should start an asynchronous operation that, when completed, will set a new value for the
	 * datastream managed by this DatastreamsSetter, but only for the device specified in the parameter.<p>
	 * Of course, it is permissible to return immediately a response, that is, a CompletableFuture already completed, but
	 * it is not permissible to block until the datastreams values are set.
	 * @param device The device where to set the value.
	 * @param value Value for the identifier returned by {@link #getDatastreamIdSatisfied}()
	 * @return A CompletableFuture that will complete with null if all goes well, or with an exception if something goes wrong.  
	 */
	CompletableFuture<Void> set(String device, Object value);
}
