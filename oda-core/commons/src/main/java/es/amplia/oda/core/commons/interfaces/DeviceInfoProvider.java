package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.entities.Software;

import java.util.List;

public interface DeviceInfoProvider {
    String getDeviceId(); //Must return the device Id of ODA
    String getApiKey();

    int getCpuTotal();
    String getClock();
    long getUptime();
    String getCpuStatus();
    int getCpuUsage();
    long getRamTotal();
    int getRamUsage();

    long getDiskTotal();
    int getDiskUsage();
    List<Software> getSoftware();
    String getTemperatureStatus();
    int getTemperatureValue();
}
