package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TemperatureStatusDatastreamGetter implements DatastreamsGetter {

	private DeviceInfoDatastreamsGetter deviceInfo;

	public TemperatureStatusDatastreamGetter(DeviceInfoDatastreamsGetter deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return DeviceInfoDatastreamsGetter.TEMPERATURE_STATUS_DATASTREAM_ID;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<CollectedValue> get(String device) {
		return CompletableFuture.completedFuture(
				new CollectedValue(System.currentTimeMillis(), Optional.ofNullable(this.deviceInfo.getTemperatureStatus()).orElse(""))
		);
	}
}
