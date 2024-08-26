package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfiguration;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DeviceInfoDatastreamsGetter implements DeviceInfoProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceInfoDatastreamsGetter.class);
    private final CommandProcessor commandProcessor;
    private final Bundle[] bundles;

    static final String DEVICE_ID_DATASTREAM_ID = "device.identifier";
    static final String SERIAL_NUMBER_DATASTREAM_ID = "device.serialNumber";
    public static final String CLOCK_DATASTREAM_ID = "device.clock";
    public static final String UPTIME_DATASTREAM_ID = "device.upTime";
    public static final String CPU_TOTAL_DATASTREAM_ID = "device.cpu.total";
    public static final String CPU_STATUS_DATASTREAM_ID = "device.cpu.status";
    public static final String CPU_USAGE_DATASTREAM_ID = "device.cpu.usage";
    public static final String RAM_TOTAL_DATASTREAM_ID = "device.ram.total";
    public static final String RAM_USAGE_DATASTREAM_ID = "device.ram.usage";
    public static final String DISK_TOTAL_DATASTREAM_ID = "device.storage.disk.total";
    public static final String DISK_USAGE_DATASTREAM_ID = "device.storage.disk.usage";
    public static final String SOFTWARE_DATASTREAM_ID = "device.software";
    public static final String TEMPERATURE_STATUS_DATASTREAM_ID = "device.temperature.status";
    public static final String TEMPERATURE_VALUE_DATASTREAM_ID = "device.temperature.value";

    static final String CLOCK_SCRIPT = "obtainClock.sh";
    static final String UPTIME_SCRIPT = "obtainUptime.sh";
    static final String CPU_TOTAL_SCRIPT = "obtainCpuTotal.sh";
    static final String CPU_STATUS_SCRIPT = "obtainCpuStatus.sh";
    static final String CPU_USAGE_SCRIPT = "obtainCpuUsage.sh";
    static final String RAM_TOTAL_SCRIPT = "obtainRamTotal.sh";
    static final String RAM_USAGE_SCRIPT = "obtainRamUsage.sh";
    static final String DISK_TOTAL_SCRIPT = "obtainDiskTotal.sh";
    static final String DISK_USAGE_SCRIPT = "obtainDiskUsage.sh";
    static final String SERIAL_NUMBER_SCRIPT = "obtainSerialNumber.sh";
    static final String TEMPERATURE_STATUS_SCRIPT = "obtainTemperatureStatus.sh";
    static final String TEMPERATURE_VALUE_SCRIPT = "obtainTemperatureValue.sh";

    private String path;

    private String deviceId;
    private String apiKey;
    private String serialNumber;


    DeviceInfoDatastreamsGetter(CommandProcessor commandProcessor, Bundle[] bundles) {
        this.commandProcessor = commandProcessor;
        this.bundles = bundles;
    }

    public void loadConfiguration(DeviceInfoConfiguration configuration) {
        deviceId = configuration.getDeviceId();
        LOGGER.info("Load new device identifier: {}", deviceId);
        apiKey = configuration.getApiKey();
        LOGGER.info("Load new API key: {}", apiKey);
        path = configuration.getPath();
        LOGGER.info("Load new path to scripts directory: {}", path);

        try {
            LOGGER.info("Preparing scripts for run");

            File dir = new File(path);
            for (File script : Objects.requireNonNull(dir.listFiles())) {
                if(!script.setExecutable(true)) {
                    LOGGER.error("Script {} couldn't be setted executable", script.getName());
                }
            }

            serialNumber = commandProcessor.execute(path + "/" + SERIAL_NUMBER_SCRIPT);
            LOGGER.info("Load new serial number: {}", serialNumber);
        } catch (CommandExecutionException ex) {
            LOGGER.error("Error executing serial number command '{}':", SERIAL_NUMBER_SCRIPT,
                    ex);
        }
    }

    
    public String getDeviceId() {
        return (deviceId != null && !deviceId.equals("")) ? deviceId : serialNumber;
    }

    
    public String getApiKey() {
        return apiKey;
    }

    
    public int getCpuTotal() {
        try {
            String cpuTotalString = commandProcessor.execute(path + "/" + CPU_TOTAL_SCRIPT);
            if (cpuTotalString != null) {
                int cpuTotal = Integer.parseInt(cpuTotalString);
                LOGGER.debug("Getting actual cores quantity: {}", cpuTotal);
                return cpuTotal;
            } else {
                LOGGER.warn("Executing CPU Total command '{}' return null", CPU_TOTAL_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing CPU Total command '{}':", CPU_TOTAL_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public String getClock() {
        try {
            String clock = commandProcessor.execute(path + "/" + CLOCK_SCRIPT);
            LOGGER.debug("Getting actual hour: {}", clock);
            return clock;
        } catch (CommandExecutionException ex) {
            LOGGER.error("Error executing Clock command '{}':", CLOCK_SCRIPT,
                    ex);
            return null;
        }
    }

    
    public long getUptime() {
        try {
            String upTimeString = commandProcessor.execute(path + "/" + UPTIME_SCRIPT);
            if (upTimeString != null) {
                long uptime = Long.parseLong(upTimeString);
                LOGGER.debug("Getting actual UpTime: {}", uptime);
                return uptime;
            } else {
                LOGGER.warn("Executing UpTime command '{}' return null", UPTIME_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing UpTime command '{}':", UPTIME_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public String getCpuStatus() {
        try {
            String cpuStatus = commandProcessor.execute(path + "/" + CPU_STATUS_SCRIPT);
            LOGGER.debug("Getting actual CPU Status: {}", cpuStatus);
            return cpuStatus;
        } catch (CommandExecutionException ex) {
            LOGGER.error("Error executing CPU Status command '{}':", CPU_STATUS_SCRIPT,
                    ex);
            return null;
        }
    }

    
    public int getCpuUsage() {
        try {
            String cpuUsageString = commandProcessor.execute(path + "/" + CPU_USAGE_SCRIPT);
            if (cpuUsageString != null) {
                int cpuUsage = Integer.parseInt(cpuUsageString);
                LOGGER.debug("Getting actual CPU Usage: {}", cpuUsage);
                return cpuUsage;
            } else {
                LOGGER.warn("Executing CPU Usage command '{}' return null", CPU_USAGE_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing CPU Usage command '{}':", CPU_USAGE_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public long getRamTotal() {
        try {
            String ramTotalString = commandProcessor.execute(path + "/" + RAM_TOTAL_SCRIPT);
            if (ramTotalString != null) {
                long ramTotal = Long.parseLong(ramTotalString);
                LOGGER.debug("Getting actual RAM Usage: {}", ramTotal);
                return ramTotal;
            } else {
                LOGGER.warn("Executing RAM Total command '{}' return null", RAM_TOTAL_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing RAM Total command '{}':", RAM_TOTAL_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public int getRamUsage() {
        try {
            String ramUsageString = commandProcessor.execute(path + "/" + RAM_USAGE_SCRIPT);
            if (ramUsageString != null) {
                int ramUsage = Integer.parseInt(ramUsageString);
                LOGGER.debug("Getting actual RAM Usage: {}", ramUsage);
                return ramUsage;
            } else {
                LOGGER.warn("Executing RAM Usage command '{}' return null", RAM_USAGE_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing RAM Usage command '{}':", RAM_USAGE_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public long getDiskTotal() {
        try {
            String diskTotalString = commandProcessor.execute(path + "/" + DISK_TOTAL_SCRIPT);
            if (diskTotalString != null) {
                long diskTotal = Long.parseLong(diskTotalString);
                LOGGER.debug("Getting actual Disk Capacity Usage: {}", diskTotal);
                return diskTotal;
            } else {
                LOGGER.warn("Executing Disk Total command '{}' return null", DISK_TOTAL_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing Disk Total command '{}':", DISK_TOTAL_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public int getDiskUsage() {
        try {
            String diskUsageString = commandProcessor.execute(path + "/" + DISK_USAGE_SCRIPT);
            if (diskUsageString != null) {
                int diskUsage = Integer.parseInt(diskUsageString);
                LOGGER.debug("Getting actual Disk Capacity Usage: {}", diskUsage);
                return diskUsage;
            } else {
                LOGGER.warn("Executing Disk Usage command '{}' return null", DISK_USAGE_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException ex) {
            LOGGER.error("Error executing Disk Usage command '{}':", DISK_USAGE_SCRIPT,
                    ex);
            return 0;
        }
    }

    
    public List<Software> getSoftware() {
        List<Software> software = new ArrayList<>();
        for (Bundle bundle: bundles) {
            software.add(new Software(bundle.getSymbolicName(), bundle.getVersion().toString(), "SOFTWARE"));
        }
        LOGGER.debug("Getting actual used Software: {}", software);
        return software;
    }

    
    public String getTemperatureStatus() {
        try {
            String temperatureStatus = commandProcessor.execute(path + "/" + TEMPERATURE_STATUS_SCRIPT);
            LOGGER.debug("Getting actual Temperature Status: {}", temperatureStatus);
            return temperatureStatus;
        } catch (CommandExecutionException ex) {
            LOGGER.error("Error executing Temperature Status command '{}':", TEMPERATURE_STATUS_SCRIPT,
                    ex);
            return null;
        }
    }

    
    public int getTemperatureValue() {
        try {
            String tempValueString = commandProcessor.execute(path + "/" + TEMPERATURE_VALUE_SCRIPT);
            if (tempValueString != null) {
                int temperatureValue = Integer.parseInt(tempValueString);
                LOGGER.debug("Getting actual Temperature: {}", temperatureValue);
                return temperatureValue;
            } else {
                LOGGER.warn("Executing Temperature command '{}' return null", TEMPERATURE_VALUE_SCRIPT);
                return 0;
            }
        } catch (CommandExecutionException | NumberFormatException ex) {
            LOGGER.error("Error executing Temperature command '{}':", TEMPERATURE_VALUE_SCRIPT,
                    ex);
            return 0;
        }
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }


    DatastreamsGetter getDatastreamsGetterForDeviceId() {
        return new DatastreamsGetter() {
            
            public String getDatastreamIdSatisfied() {
                return DEVICE_ID_DATASTREAM_ID;
            }
            
            public List<String> getDevicesIdManaged() {
                return Collections.singletonList("");
            }
            
            public CompletableFuture<CollectedValue> get(String device) {
                return CompletableFuture.completedFuture(
                    new CollectedValue(System.currentTimeMillis(), getDeviceId())
                );
            }
        };
    }

    DatastreamsGetter getDatastreamsGetterForSerialNumber() {
        return new DatastreamsGetter() {
            
            public String getDatastreamIdSatisfied() {
                return SERIAL_NUMBER_DATASTREAM_ID;
            }

            public List<String> getDevicesIdManaged() {
                return Collections.singletonList("");
            }

            public CompletableFuture<CollectedValue> get(String device) {
                return CompletableFuture.completedFuture(
                    new CollectedValue(System.currentTimeMillis(), getSerialNumber())
                );
            }
        };
    }
}
