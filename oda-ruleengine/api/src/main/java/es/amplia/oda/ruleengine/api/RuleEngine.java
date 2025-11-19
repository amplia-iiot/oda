package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.OsgiContext;

public interface RuleEngine {
	State engine(State state, DatastreamValue value, OsgiContext ctx);

	void createDatastreamDirectory(String nameRule);

	void deleteDatastreamDirectory(String nameRule);

	void createRule(String nameRule);

	void modifyRule(String nameRule);

	void deleteRule(String nameRule);

	void reloadAllRules();

	void stop();
}
