package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.core.commons.utils.DatastreamValue;

public interface RuleEngine {
	/**
	 *
	 * @param state
	 * @param value
	 */
	State engine(State state, DatastreamValue value) ;

	void createDatastreamDirectory(String nameRule);

	void deleteDatastreamDirectory(String nameRule);

	/**
	 *
	 * @param nameRule
	 */
	void createRule(String nameRule);

	/**
	 *
	 * @param nameRule
	 */
	void deleteRule(String nameRule);

	/**
	 *
	 */
	void stop();
}
