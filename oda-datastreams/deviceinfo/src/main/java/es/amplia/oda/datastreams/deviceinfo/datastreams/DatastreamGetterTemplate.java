package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatastreamGetterTemplate implements DatastreamsGetter {

	private String datastreamId;
	private String script;
	private String type;
	private GetValue getterFunction;

	public DatastreamGetterTemplate(String datastreamId, String script, GetValue getterFunction) {
		this(datastreamId, script, null, getterFunction);
	}

	public DatastreamGetterTemplate(String datastreamId, String script, String type, GetValue getterFunction) {
		this.datastreamId = datastreamId;
		this.getterFunction = getterFunction;
		this.script = script;
		this.type = type;
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
				new CollectedValue(System.currentTimeMillis(), getterFunction.op(script, type)));
	}
}
