package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
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
    public ScadaInfo translate(DatastreamInfo info) {
        Optional<ScadaInfo> scadaInfo =
                Optional.ofNullable(proxy.callFirst(translator -> translator.translate(info)));
        return scadaInfo.orElseThrow(() ->
                new DataNotFoundException(String.format("SCADA info for (%s) not found", info)));
    }

    @Override
    public Object transformValue(int address, Object type, Object value) {
        return proxy.callFirst(translator -> translator.transformValue(address, type, value));
    }

    @Override
    public ScadaTranslationInfo getTranslationInfo(ScadaInfo info) {
        Optional<ScadaTranslationInfo> datastreamInfo =
                Optional.ofNullable(proxy.callFirst(translator -> translator.getTranslationInfo(info)));
        return datastreamInfo
                .orElseThrow(() -> new DataNotFoundException("Datastream info not found for SCADA info " + info));
    }
	
	@Override
	public List<String> getDatastreamsIds() {
		Optional<List<String>> datastreamsIds =
                Optional.ofNullable(proxy.callFirst(ScadaTableTranslator::getDatastreamsIds));
        return datastreamsIds.orElseThrow(() ->
                new DataNotFoundException("Datastreams IDs not found"));
	}

    @Override
    public List<String> getDeviceIds() {
        Optional<List<String>> deviceIds =
                Optional.ofNullable(proxy.callFirst(ScadaTableTranslator::getDeviceIds));
        return deviceIds.orElseThrow(() ->
                new DataNotFoundException("Device IDs not found"));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
