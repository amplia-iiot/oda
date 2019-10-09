package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcService;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class AdcServiceProxy implements AdcService, AutoCloseable {

	private final OsgiServiceProxy<AdcService> proxy;

	public AdcServiceProxy(BundleContext bundleContext) {
		this.proxy = new OsgiServiceProxy<>(AdcService.class, bundleContext);
	}

	private <T> T returnValueOrExceptionIfNull(Supplier<T> supplier) {
		Optional<T> optional = Optional.ofNullable(supplier.get());
		return  optional.orElseThrow(() -> new AdcDeviceException("No ADC service available"));
	}

	@Override
	public AdcChannel getChannelByName(String channelName) throws AdcDeviceException {
		return returnValueOrExceptionIfNull(() -> proxy.callFirst(adcService -> adcService.getChannelByName(channelName)));
	}

	@Override
	public AdcChannel getChannelByIndex(int index) throws AdcDeviceException {
		return returnValueOrExceptionIfNull(() -> proxy.callFirst(adcService -> adcService.getChannelByIndex(index)));
	}

	@Override
	public Map<Integer, AdcChannel> getAvailableChannels() {
		return returnValueOrExceptionIfNull(() -> proxy.callFirst(AdcService::getAvailableChannels));
	}

	@Override
	public void close() throws Exception {
		proxy.close();
	}
}
