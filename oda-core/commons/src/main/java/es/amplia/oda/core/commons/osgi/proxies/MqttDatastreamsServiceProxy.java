package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import org.osgi.framework.BundleContext;

import java.util.Optional;

public class MqttDatastreamsServiceProxy implements MqttDatastreamsService, AutoCloseable {

	private final OsgiServiceProxy<MqttDatastreamsService> proxy;

	public MqttDatastreamsServiceProxy(BundleContext bundleContext) {
		this.proxy = new OsgiServiceProxy<>(MqttDatastreamsService.class, bundleContext);
	}

	@Override
	public boolean isReady() {
		return Optional.ofNullable(proxy.callFirst(MqttDatastreamsService::isReady))
				.orElse(false);
	}

	@Override
	public void close() {
		proxy.close();
	}
}
