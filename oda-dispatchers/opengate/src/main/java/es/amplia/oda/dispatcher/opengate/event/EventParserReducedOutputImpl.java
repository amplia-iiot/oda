package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import java.util.Collections;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class EventParserReducedOutputImpl implements EventParser {

    private final DeviceInfoProvider deviceInfoProvider;


    EventParserReducedOutputImpl(DeviceInfoProvider deviceInfoProvider) {
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Override
    public OutputDatastream parse(Event event) {
        String deviceId = getDeviceId(event);
        Datapoint datapoint = new Datapoint(null, event.getValue());
        Datastream datastream = new Datastream(event.getDatastreamId(), Collections.singleton(datapoint));

        return new OutputDatastream(OPENGATE_VERSION, deviceId, null, Collections.singleton(datastream));
    }

    private String getDeviceId(Event event) {
        String hostId = deviceInfoProvider.getDeviceId();
        String deviceId = event.getDeviceId();
        String inferredDeviceId = "".equals(deviceId) ? hostId : deviceId;
        return inferredDeviceId.equals(hostId) ? null : inferredDeviceId;
    }
}
