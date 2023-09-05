package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;

import org.osgi.framework.BundleContext;

import javax.script.Invocable;
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
    public Object transformValue(Invocable script, Object value) {
        return proxy.callFirst(translator -> translator.transformValue(script, value));
    }

    @Override
    public DatastreamInfo getDatastreamInfo(ScadaInfo info) {
        Optional<DatastreamInfo> datastreamInfo =
                Optional.ofNullable(proxy.callFirst(translator -> translator.getDatastreamInfo(info)));
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
    public void close() {
        proxy.close();
    }
}
