package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class SchedulerImpl implements Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerImpl.class);

    private final DeviceInfoProvider deviceInfoProvider;
    private final EventCollector collector;
    private final OpenGateConnector connector;
    private final Serializer serializer;

    SchedulerImpl(DeviceInfoProvider deviceInfoProvider, EventCollector collector, OpenGateConnector connector,
                  Serializer serializer) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.collector = collector;
        this.connector = connector;
        this.serializer = serializer;
    }

    @Override
    public void runFor(Set<String> ids) {
        LOGGER.debug("runFor({})", ids);
        Map<String, OutputDatastream> devicesToIotData = new HashMap<>();
        
        for(String id: ids) {
            List<Event> recollectedValues = collector.getAndCleanCollectedValues(id);
            if(recollectedValues != null) {
                for(Event collectedValue: recollectedValues) {
                    String deviceId = collectedValue.getDeviceId().equals("") ?
                            deviceInfoProvider.getDeviceId() : collectedValue.getDeviceId();
                    Datastream ds = locateDatapointList(devicesToIotData, collectedValue, deviceId);
                    Datapoint dp = new Datapoint(collectedValue.getAt(), collectedValue.getValue());
                    ds.getDatapoints().add(dp);
                }
            } else {
                LOGGER.info("No value recollected for Datastream {}", id);
            }
        }

            for(OutputDatastream data: devicesToIotData.values()) {
                try {
                    byte[] payload = serializer.serialize(data);
                    connector.uplink(payload);
                } catch (IOException e) {
                    LOGGER.error("Data from Dev:" + data.getDevice() +
                            " Stream:" + data.getDatastreams() +
                            " couldn't be serialized");
                }
            }
    }

    private Datastream locateDatapointList(Map<String, OutputDatastream> devicesToIotData, Event data,
                                                  String deviceId) {
        String datastreamId = data.getDatastreamId();

        String[] path = getPath(deviceInfoProvider.getDeviceId(), deviceId, data.getPath());
        devicesToIotData.putIfAbsent(deviceId, new OutputDatastream(OPENGATE_VERSION, deviceId, path, new HashSet<>()));

        OutputDatastream iotData = devicesToIotData.get(deviceId);

        return iotData.getDatastreams().stream()
                .filter(ds->ds.getId().equals(datastreamId)).findFirst()
                .orElseGet(() -> {
                            Datastream datastream = new Datastream(datastreamId, new HashSet<>());
                            iotData.getDatastreams().add(datastream);
                            return datastream;
                });
    }

    private String[] getPath(String hostId, String deviceId, String[] path) {
        if (hostId == null) {
            return null;
        } else if (hostId.equals(deviceId)) {
            return path;
        } else if (path == null) {
            return new String[] { hostId };
        } else {
            List<String> pathDevices = Arrays.stream(path).collect(Collectors.toList());
            pathDevices.add(0, hostId);
            return pathDevices.toArray(new String[0]);
        }
    }
}
