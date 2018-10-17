package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.List;

public interface DatastreamsGettersLocator {
	/**
	 * This function must return all the DatastreamsGetter currently in the system.
	 * In the future, maybe we can pass parameters so that the returned list is filtered of not useful elements.
	 * But right now the DatastreamsGetters are registered in Osgi without filters, so it can not be done automatically.  
	 * @return The list of DatastreamsGetters currently in the system.
	 */
	List<DatastreamsGetter> getDatastreamsGetters();
}
