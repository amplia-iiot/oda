package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Optional;

public class ScadaTableTranslatorProxy implements ScadaTableTranslator, AutoCloseable {

    private final OsgiServiceProxy<ScadaTableTranslator> proxy;

    public ScadaTableTranslatorProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(ScadaTableTranslator.class, bundleContext);
    }

    @Override
    public ScadaInfo translate(DatastreamInfo info, boolean isEvent) {
        Optional<ScadaInfo> scadaInfo =
                Optional.ofNullable(proxy.callFirst(translator -> translator.translate(info, isEvent)));
        return scadaInfo.orElse(null);
    }

    @Override
    public Object transformValue(int address, Object type, boolean isEvent, Object value) {
        return proxy.callFirst(translator -> translator.transformValue(address, type, isEvent, value));
    }

    @Override
    public ScadaTranslationInfo getTranslationInfo(ScadaInfo info, boolean isEvent) {
        Optional<ScadaTranslationInfo> datastreamInfo =
                Optional.ofNullable(proxy.callFirst(translator -> translator.getTranslationInfo(info, isEvent)));
        return datastreamInfo.orElse(null);
    }
	
	@Override
	public List<String> getRecollectionDatastreamsIds() {
		Optional<List<String>> datastreamsIds =
                Optional.ofNullable(proxy.callFirst(ScadaTableTranslator::getRecollectionDatastreamsIds));
        return datastreamsIds.orElse(null);
	}

    @Override
    public List<String> getRecollectionDeviceIds() {
        Optional<List<String>> deviceIds =
                Optional.ofNullable(proxy.callFirst(ScadaTableTranslator::getRecollectionDeviceIds));
        return deviceIds.orElse(null);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
