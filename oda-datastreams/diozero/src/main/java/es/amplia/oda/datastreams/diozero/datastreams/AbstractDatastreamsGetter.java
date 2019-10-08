package es.amplia.oda.datastreams.diozero.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class AbstractDatastreamsGetter implements DatastreamsGetter {

	private final String datastreamId;
	private final Executor executor;

	protected AbstractDatastreamsGetter(String datastreamId, Executor executor) {
		this.datastreamId = datastreamId;
		this.executor = executor;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return datastreamId;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<DatastreamsGetter.CollectedValue> get(String device) {
		return CompletableFuture.supplyAsync(() -> getDatastreamIdValuesForDevicePattern(device), executor);
	}

	protected String getDatastreamId() {
		return this.datastreamId;
	}

	protected abstract DatastreamsGetter.CollectedValue getDatastreamIdValuesForDevicePattern(String device);
}
