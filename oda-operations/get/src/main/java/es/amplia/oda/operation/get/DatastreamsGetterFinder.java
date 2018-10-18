package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import lombok.Data;

import java.util.List;
import java.util.Set;

public interface DatastreamsGetterFinder {
	@Data
	class Return
	{
		private final List<DatastreamsGetter> getters;
		private final Set<String> notFoundIds;
	}
	
	/**
	 * This function will try to find the DatastreamsGetters that can generate values for the parameters specified.
	 * 
	 * @param deviceId The deviceId that the returned DatastreamsGetters must manage. Empty string means the ODA device itself. 
	 * @param datastreamIdentifiers The Datastream identifiers that the returned DatastreamsGetters must generate. Not null.
	 * @return A list with all the datastreams found that will generate values for the parameters specified, and a set
	 *  of identifiers that no DatastreamsGetter manage.
	 */
	Return getGettersSatisfying(String deviceId, Set<String> datastreamIdentifiers);
}
