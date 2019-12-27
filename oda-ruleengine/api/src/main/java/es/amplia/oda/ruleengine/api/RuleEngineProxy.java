package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import org.osgi.framework.BundleContext;

public class RuleEngineProxy implements RuleEngine, AutoCloseable {

	private final OsgiServiceProxy<RuleEngine> proxy;

	public RuleEngineProxy(BundleContext bundleContext) {
		this.proxy = new OsgiServiceProxy<>(RuleEngine.class, bundleContext);
	}

	@Override
	public State engine(State state, DatastreamValue value) {
		return proxy.callFirst(ruleEngine -> ruleEngine.engine(state, value));
	}

	@Override
	public void createDatastreamDirectory(String nameRule) {
		proxy.consumeFirst(ruleEngine -> ruleEngine.createDatastreamDirectory(nameRule));
	}

	@Override
	public void deleteDatastreamDirectory(String nameRule) {
		proxy.consumeFirst(ruleEngine -> ruleEngine.deleteDatastreamDirectory(nameRule));
	}

	@Override
	public void createRule(String nameRule) {
		proxy.consumeFirst(ruleEngine -> ruleEngine.createRule(nameRule));
	}

	@Override
	public void deleteRule(String nameRule) {
		proxy.consumeFirst(ruleEngine -> ruleEngine.deleteRule(nameRule));
	}

	@Override
	public void stop() {
		proxy.consumeFirst(RuleEngine::stop);
	}

	@Override
	public void close() {
		proxy.close();
	}
}
