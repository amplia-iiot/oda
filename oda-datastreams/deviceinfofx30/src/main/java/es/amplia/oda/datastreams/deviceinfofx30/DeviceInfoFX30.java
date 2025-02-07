package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30Configuration;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceInfoFX30 implements DeviceInfoProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceInfoFX30.class);

	private final CommandProcessor commandProcessor;
	private final Bundle[] bundles;

	static final String DEVICE_ID_DATASTREAM_ID = "deviceId";
	static final String SERIAL_NUMBER_DATASTREAM_ID = "device.serialNumber";
	static final String MAKER_DATASTREAM_ID = "device.maker";
	static final String MODEL_DATASTREAM_ID = "device.model";
	static final String IMEI_DATASTREAM_ID = "device.communicationModules[].mobile.imei";
	static final String IMSI_DATASTREAM_ID = "device.communicationModules[].subscription.mobile.imsi";
	static final String ICC_DATASTREAM_ID = "device.communicationModules[].subscriber.mobile.icc";
	static final String RSSI_DATASTREAM_ID = "device.communicationModules[].subscription.mobile.signalStrength";
	static final String SOFTWARE_DATASTREAM_ID = "device.software";
	static final String APN_DATASTREAM_ID = "device.apn";
	static final String CLOCK_DATASTREAM_ID = "device.clock";
	static final String UPTIME_DATASTREAM_ID = "device.upTime";
	static final String TEMPERATURE_VALUE_DATASTREAM_ID = "device.temperature.value";
	static final String TEMPERATURE_STATUS_DATASTREAM_ID = "device.temperature.status";
	static final String CPU_STATUS_DATASTREAM_ID = "device.cpu.status";
	static final String CPU_USAGE_DATASTREAM_ID = "device.cpu.usage";
	static final String CPU_TOTAL_DATASTREAM_ID = "device.cpu.total";
	static final String RAM_USAGE_DATASTREAM_ID = "device.ram.usage";
	static final String RAM_TOTAL_DATASTREAM_ID = "device.ram.total";
	static final String DISK_USAGE_DATASTREAM_ID = "device.disk.usage";
	static final String DISK_TOTAL_DATASTREAM_ID = "device.disk.total";

	static final String SERIAL_NUMBER_SCRIPT = "obtainSerialNumber.sh";
	static final String MODEL_SCRIPT = "obtainModel.sh";
	static final String IMEI_SCRIPT = "obtainImei.sh";
	static final String IMSI_SCRIPT = "obtainImsi.sh";
	static final String ICC_SCRIPT = "obtainIcc.sh";
	static final String RSSI_SCRIPT = "obtainRssi.sh";
	static final String SOFTWARE_SCRIPT = "obtainSoftware.sh";
	static final String APN_SCRIPT = "obtainApn.sh";
	static final String CLOCK_SCRIPT = "obtainClock.sh";
	static final String UPTIME_SCRIPT = "obtainUptime.sh";
	static final String TEMPERATURE_VALUE_SCRIPT = "obtainTemperatureValue.sh";
	static final String TEMPERATURE_STATUS_SCRIPT = "obtainTemperatureStatus.sh";
	static final String CPU_STATUS_SCRIPT = "obtainCpuStatus.sh";
	static final String CPU_USAGE_SCRIPT = "obtainCpuUsage.sh";
	static final String CPU_TOTAL_SCRIPT = "obtainCpuTotal.sh";
	static final String RAM_USAGE_SCRIPT = "obtainRamUsage.sh";
	static final String RAM_TOTAL_SCRIPT = "obtainRamTotal.sh";
	static final String DISK_USAGE_SCRIPT = "obtainDiskUsage.sh";
	static final String DISK_TOTAL_SCRIPT = "obtainDiskTotal.sh";

	private String deviceId;
	private String apiKey;
	private String path;
	private String serialNumber;

	DeviceInfoFX30(CommandProcessor commandProcessor, Bundle[] bundles) {
		this.commandProcessor = commandProcessor;
		this.bundles = bundles;
	}

	public void loadConfiguration(DeviceInfoFX30Configuration configuration) {
		deviceId = configuration.getDeviceId();
		LOGGER.info("Load new device identifier: {}", deviceId);
		apiKey = configuration.getApiKey();
		LOGGER.info("Load new API key: {}", apiKey);
		path = configuration.getPath();
		LOGGER.info("Load new path of scripts: {}", path);
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
			LOGGER.error("Error executing command '{}': ", SERIAL_NUMBER_SCRIPT,
					ex);
		}
	}

	String getSerialNumber() {
		try {
			serialNumber = commandProcessor.execute(path + "/" + SERIAL_NUMBER_SCRIPT);
			return serialNumber;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing Serial Number command '{}': ", SERIAL_NUMBER_SCRIPT, ex);
			return null;
		}
	}
	@Override
	public String getDeviceId() {
		if (deviceId == null || deviceId.equals("")) {
			deviceId = getSerialNumber();
		}
		return deviceId;
	}

	@Override
	public void setDeviceId(String deviceId) {
        LOGGER.error("This bundle cannot set DeviceId");
    }

	@Override
	public String getApiKey() {
		return apiKey;
	}

	String getPath() {
		return path;
	}

	String getModel() {
		try {
			String model = commandProcessor.execute(path + "/" + MODEL_SCRIPT);
			LOGGER.debug("Getting actual model of device: {}", model);
			return model;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing Model command '{}': ", MODEL_SCRIPT,
					ex);
			return null;
		}
	}

	String getImei() {
		try {
			String imei = commandProcessor.execute(path + "/" + IMEI_SCRIPT);
			LOGGER.debug("Getting actual imei of device: {}", imei);
			return imei;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing IMEI command '{}': ", IMEI_SCRIPT,
					ex);
			return null;
		}
	}

	String getImsi() {
		try {
			String imsi = commandProcessor.execute(path + "/" + IMSI_SCRIPT);
			LOGGER.debug("Getting actual imsi of device: {}", imsi);
			return imsi;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing IMSI command '{}': ", IMSI_SCRIPT,
					ex);
			return null;
		}
	}

	String getIcc() {
		try {
			String icc = commandProcessor.execute(path + "/" + ICC_SCRIPT);
			LOGGER.debug("Getting actual icc of device: {}", icc);
			return icc;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing ICC command '{}': ", ICC_SCRIPT,
					ex);
			return null;
		}
	}

	String getRssi() {
		try {
			String rssi = commandProcessor.execute(path + "/" + RSSI_SCRIPT);
			LOGGER.debug("Getting actual rssi of device: {}", rssi);
			return rssi;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing RSSI command '{}': ", ICC_SCRIPT,
					ex);
			return null;
		}
	}

	@Override
	public List<Software> getSoftware() {
		try {
			List<Software> software = new ArrayList<>();
			String[] versions = commandProcessor.execute(path + "/" + SOFTWARE_SCRIPT).split(" && ");
			for (String version : versions) {
				if(version.contains("Firmware") || version.contains("Bootloader"))
					software.add(parseSoftware(version, "FIRMWARE"));
				else
					software.add(parseSoftware(version, "SOFTWARE"));
			}
			for (Bundle bundle: bundles) {
				software.add(new Software(bundle.getSymbolicName(), bundle.getVersion().toString(), "SOFTWARE"));
			}
			LOGGER.debug("Getting actual used Software: {}", software);
			return software;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing Disk Usage command '{}': ", SOFTWARE_SCRIPT,
					ex);
			return null;
		}
	}

	String getApn() {
		try {
			String apn = commandProcessor.execute(path + "/" + APN_SCRIPT);
			LOGGER.debug("Getting actual apn of device: {}", apn);
			return apn;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing APN command '{}': ", APN_SCRIPT,
					ex);
			return null;
		}
	}

	public String getClock() {
		try {
			String clock = commandProcessor.execute(path + "/" + CLOCK_SCRIPT);
			LOGGER.debug("Getting actual hour: {}", clock);
			return clock;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing Clock command '{}': ", CLOCK_SCRIPT,
					ex);
			return null;
		}
	}

	public Long getUptime() {
		try {
			long uptime = Long.parseLong(commandProcessor.execute(path + "/" + UPTIME_SCRIPT));
			LOGGER.debug("Getting actual UpTime: {}", uptime);
			return uptime;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing UpTime command '{}': ", UPTIME_SCRIPT,
					ex);
			return null;
		}
	}

	public Integer getTemperatureValue() {
		try {
			int temperatureValue = Integer.parseInt(commandProcessor.execute(path + "/" + TEMPERATURE_VALUE_SCRIPT));
			LOGGER.debug("Getting actual Temperature: {}", temperatureValue);
			return temperatureValue;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing Temperature command '{}': ", TEMPERATURE_VALUE_SCRIPT,
					ex);
			return null;
		}
	}

	public String getTemperatureStatus() {
		try {
			String temperatureStatus = commandProcessor.execute(path + "/" + TEMPERATURE_STATUS_SCRIPT);
			LOGGER.debug("Getting actual Temperature Status: {}", temperatureStatus);
			return temperatureStatus;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing Temperature Status command '{}': ", TEMPERATURE_STATUS_SCRIPT,
					ex);
			return null;
		}
	}

	public String getCpuStatus() {
		try {
			String cpuStatus = commandProcessor.execute(path + "/" + CPU_STATUS_SCRIPT);
			LOGGER.debug("Getting actual CPU Status: {}", cpuStatus);
			return cpuStatus;
		} catch (CommandExecutionException ex) {
			LOGGER.error("Error executing CPU Status command '{}': ", CPU_STATUS_SCRIPT,
					ex);
			return null;
		}
	}

	public Integer getCpuUsage() {
		try {
			int cpuUsage = Integer.parseInt(commandProcessor.execute(path + "/" + CPU_USAGE_SCRIPT));
			LOGGER.debug("Getting actual CPU Usage: {}", cpuUsage);
			return cpuUsage;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing CPU Usage command '{}': ", CPU_USAGE_SCRIPT,
					ex);
			return null;
		}
	}

	public Integer getCpuTotal() {
		try {
			int cpuTotal = Integer.parseInt(commandProcessor.execute(path + "/" + CPU_TOTAL_SCRIPT));
			LOGGER.debug("Getting actual cores quantity: {}", cpuTotal);
			return cpuTotal;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing cores command '{}': ", CPU_TOTAL_SCRIPT,
					ex);
			return null;
		}
	}

	public Integer getRamUsage() {
		try {
			int ramUsage = Integer.parseInt(commandProcessor.execute(path + "/" + RAM_USAGE_SCRIPT));
			LOGGER.debug("Getting actual RAM Usage: {}", ramUsage);
			return ramUsage;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing RAM Usage command '{}': ", RAM_USAGE_SCRIPT,
					ex);
			return null;
		}
	}

	public Long getRamTotal() {
		try {
			long ramTotal = Long.parseLong(commandProcessor.execute(path + "/" + RAM_TOTAL_SCRIPT));
			LOGGER.debug("Getting actual RAM Usage: {}", ramTotal);
			return ramTotal;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing RAM Total command '{}': ", RAM_TOTAL_SCRIPT,
					ex);
			return null;
		}
	}

	public Integer getDiskUsage() {
		try {
			int diskUsage = Integer.parseInt(commandProcessor.execute(path + "/" + DISK_USAGE_SCRIPT));
			LOGGER.debug("Getting actual Disk Capacity Usage: {}", diskUsage);
			return diskUsage;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing Disk Usage command '{}': ", DISK_USAGE_SCRIPT,
					ex);
			return null;
		}
	}

	public Long getDiskTotal() {
		try {
			long diskTotal = Long.parseLong(commandProcessor.execute(path + "/" + DISK_TOTAL_SCRIPT));
			LOGGER.debug("Getting actual Disk Capacity Usage: {}", diskTotal);
			return diskTotal;
		} catch (CommandExecutionException | NumberFormatException ex) {
			LOGGER.error("Error executing Disk Total command '{}': ", DISK_TOTAL_SCRIPT,
					ex);
			return null;
		}
	}

	private Software parseSoftware(String value, String type) {
		String[] values = value.split(":");
		return new Software(values[0], values[1], type);
	}
}
