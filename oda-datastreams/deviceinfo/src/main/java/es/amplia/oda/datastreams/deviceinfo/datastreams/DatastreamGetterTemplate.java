package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatastreamGetterTemplate implements DatastreamsGetter {

	private String datastreamId;
	private GetValue getterFunction;

	public DatastreamGetterTemplate(String datastreamId, GetValue getterFunction) {
		this.datastreamId = datastreamId;
		this.getterFunction = getterFunction;
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
	public CompletableFuture<CollectedValue> get(String device) {
		return CompletableFuture.completedFuture(
				new CollectedValue(System.currentTimeMillis(), getterFunction.op()));
	}
}
