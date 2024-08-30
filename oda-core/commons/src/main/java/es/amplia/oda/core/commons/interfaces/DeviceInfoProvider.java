package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.entities.Software;

import java.util.List;

public interface DeviceInfoProvider {
    String getDeviceId(); //Must return the device Id of ODA
    String getApiKey();

    Integer getCpuTotal();
    String getClock();
    Long getUptime();
    String getCpuStatus();
    Integer getCpuUsage();
    Long getRamTotal();
    Integer getRamUsage();

    Long getDiskTotal();
    Integer getDiskUsage();
    List<Software> getSoftware();
    String getTemperatureStatus();
    Integer getTemperatureValue();
}
