package es.amplia.oda.datastreams.deviceinfofx30.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DeviceIdDatastreamGetter implements DatastreamsGetter {

	private DeviceInfoFX30 deviceInfo;

	public DeviceIdDatastreamGetter(DeviceInfoFX30 deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return DeviceInfoFX30.DEVICE_ID_DATASTREAM_ID;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<CollectedValue> get(String device) {
		return CompletableFuture.completedFuture(
				new CollectedValue(System.currentTimeMillis(), Optional.ofNullable(this.deviceInfo.getDeviceId()).orElse(""))
		);
	}
}
