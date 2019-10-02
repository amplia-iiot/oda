package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UptimeDatastreamGetter implements DatastreamsGetter {

	private DeviceInfoDatastreamsGetter deviceInfo;

	public UptimeDatastreamGetter(DeviceInfoDatastreamsGetter deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return DeviceInfoDatastreamsGetter.UPTIME_DATASTREAM_ID;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<CollectedValue> get(String device) {
		try {
			return CompletableFuture.completedFuture(
					new CollectedValue(System.currentTimeMillis(), Optional.of(this.deviceInfo.getUptime()).orElse(0L))
			);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
