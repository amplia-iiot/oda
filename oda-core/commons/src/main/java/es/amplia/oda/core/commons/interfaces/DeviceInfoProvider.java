package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.entities.Software;

import java.util.List;

public interface DeviceInfoProvider {
    String getDeviceId(); //Must return the device Id of ODA
    String getApiKey();
    List<Software> getSoftware();
}
