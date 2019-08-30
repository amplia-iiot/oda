package es.amplia.oda.datastreams.deviceinfo.datastreams;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DiskTotalDatastreamGetter  implements DatastreamsGetter {

	private DeviceInfoDatastreamsGetter deviceInfo;

	public DiskTotalDatastreamGetter(DeviceInfoDatastreamsGetter deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return DeviceInfoDatastreamsGetter.DISK_TOTAL_DATASTREAM_ID;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<CollectedValue> get(String device) {
		try {
			return CompletableFuture.completedFuture(
					new CollectedValue(System.currentTimeMillis(), Optional.of(this.deviceInfo.getDiskTotal()).orElse(0L))
			);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}