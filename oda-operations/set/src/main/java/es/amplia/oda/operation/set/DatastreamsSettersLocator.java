package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import java.util.List;

interface DatastreamsSettersLocator {
	/**
	 * This function must return all the DatastreamsSetter currently in the system. It is useful for Test purposes.
	 * In the future, maybe we can pass parameters so that the returned list is filtered of not useful elements.
	 * But right now the DatastreamsSetters are registered in Osgi without filters, so it can not be done automatically.  
	 * @return The list of DatastreamsSetters currently in the system.
	 */
	List<DatastreamsSetter> getDatastreamsSetters();
}
