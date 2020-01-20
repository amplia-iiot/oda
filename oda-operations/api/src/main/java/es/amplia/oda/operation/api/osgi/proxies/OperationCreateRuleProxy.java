/*package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationCreateRule;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OperationCreateRuleProxy implements OperationCreateRule, AutoCloseable{

	private final OsgiServiceProxy<OperationCreateRule> proxy;

	public OperationCreateRuleProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationCreateRule.class, bundleContext);
	}

	@Override
	public CompletableFuture<Result> createRule(String deviceId, Map<String, String> ruleInfo) {
		return proxy.callFirst(op -> op.createRule(deviceId, ruleInfo));
	}

	@Override
	public void close() {
		proxy.close();
	}
}*/
