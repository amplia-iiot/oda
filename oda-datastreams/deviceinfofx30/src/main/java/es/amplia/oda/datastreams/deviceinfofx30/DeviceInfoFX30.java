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

	private static final Logger logger = LoggerFactory.getLogger(DeviceInfoFX30.class);

	private final CommandProcessor commandProcessor;
	private final Bundle[] bundles;

	public static final String DEVICE_ID_DATASTREAM_ID = "deviceId";
	public static final String SERIAL_NUMBER_DATASTREAM_ID = "device.serialNumber";
	public static final String MAKER_DATASTREAM_ID = "device.maker";
	public static final String MODEL_DATASTREAM_ID = "device.model";
	public static final String IMEI_DATASTREAM_ID = "device.communicationModules[].mobile.imei";
	public static final String IMSI_DATASTREAM_ID = "device.communicationModules[].subscription.mobile.imsi";
	public static final String ICC_DATASTREAM_ID = "device.communicationModules[].subscriber.mobile.icc";
	public static final String RSSI_DATASTREAM_ID = "device.communicationModules[].subscription.mobile.signalStrength";
	public static final String SOFTWARE_DATASTREAM_ID = "device.software";
	public static final String IP_PRESENCE_DATASTREAM_ID = "";
	public static final String IP_ADDRESS_DATASTREAM_ID = "device.communicationModules[].subscription.address";
	public static final String APN_DATASTREAM_ID = "";
	public static final String CLOCK_DATASTREAM_ID = "device.clock";
	public static final String UPTIME_DATASTREAM_ID = "device.upTime";
	public static final String TEMPERATURE_VALUE_DATASTREAM_ID = "device.temperature.value";
	public static final String TEMPERATURE_STATUS_DATASTREAM_ID = "device.temperature.status";
	public static final String CPU_STATUS_DATASTREAM_ID = "device.cpu.status";
	public static final String CPU_USAGE_DATASTREAM_ID = "device.cpu.usage";
	public static final String CPU_TOTAL_DATASTREAM_ID = "device.cpu.total";
	public static final String RAM_USAGE_DATASTREAM_ID = "device.ram.usage";
	public static final String RAM_TOTAL_DATASTREAM_ID = "device.ram.total";
	public static final String DISK_USAGE_DATASTREAM_ID = "device.disk.usage";
	public static final String DISK_TOTAL_DATASTREAM_ID = "device.disk.total";

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
		String error = "";
		deviceId = configuration.getDeviceId();
		logger.info("Load new device identifier: {}", deviceId);
		apiKey = configuration.getApiKey();
		logger.info("Load new API key: {}", apiKey);
		path = configuration.getPath();
		logger.info("Load new path of scripts: {}", path);
		try {
			error = "Chmod to prepare scripts";
			logger.info("Preparing scripts for run");

			File dir = new File(path);
			for (File script : Objects.requireNonNull(dir.listFiles())) {
				script.setExecutable(true);
			}

			error = SERIAL_NUMBER_SCRIPT;
			serialNumber = commandProcessor.execute(path + "/" + SERIAL_NUMBER_SCRIPT);
			logger.info("Load new serial number: {}", serialNumber);
		} catch (CommandExecutionException ex) {
			logger.error("Error executing command '{}': {}", error,
					ex);
		}
	}

	public String getSerialNumber() {
		try {
			serialNumber = commandProcessor.execute(path + "/" + SERIAL_NUMBER_SCRIPT);
			logger.info("Getting actual hour: {}", serialNumber);
			return serialNumber;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Serial Number command '{}': {}", SERIAL_NUMBER_SCRIPT,
					ex);
			return null;
		}
	}
	@Override
	public String getDeviceId() {
		return deviceId != null ? deviceId : getSerialNumber();
	}

	@Override
	public String getApiKey() {
		return apiKey;
	}

	String getPath() {
		return path;
	}

	public String getModel() {
		try {
			String model = commandProcessor.execute(path + "/" + MODEL_SCRIPT);
			logger.info("Getting actual model of device: {}", model);
			return model;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Model command '{}': {}", MODEL_SCRIPT,
					ex);
			return null;
		}
	}

	public String getImei() {
		try {
			String imei = commandProcessor.execute(path + "/" + IMEI_SCRIPT);
			logger.info("Getting actual imei of device: {}", imei);
			return imei;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing IMEI command '{}': {}", IMEI_SCRIPT,
					ex);
			return null;
		}
	}

	public String getImsi() {
		try {
			String imsi = commandProcessor.execute(path + "/" + IMSI_SCRIPT);
			logger.info("Getting actual imsi of device: {}", imsi);
			return imsi;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing IMSI command '{}': {}", IMSI_SCRIPT,
					ex);
			return null;
		}
	}

	public String getIcc() {
		try {
			String icc = commandProcessor.execute(path + "/" + ICC_SCRIPT);
			logger.info("Getting actual icc of device: {}", icc);
			return icc;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing ICC command '{}': {}", ICC_SCRIPT,
					ex);
			return null;
		}
	}

	public String getRssi() {
		try {
			String rssi = commandProcessor.execute(path + "/" + RSSI_SCRIPT);
			logger.info("Getting actual rssi of device: {}", rssi);
			return rssi;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing RSSI command '{}': {}", ICC_SCRIPT,
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
			logger.info("Getting actual used Software: {}", software);
			return software;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Disk Usage command '{}': {}", SOFTWARE_SCRIPT,
					ex);
			return null;
		}
	}

	public String getApn() {
		try {
			String apn = commandProcessor.execute(path + "/" + APN_SCRIPT);
			logger.info("Getting actual apn of device: {}", apn);
			return apn;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing APN command '{}': {}", APN_SCRIPT,
					ex);
			return null;
		}
	}

	@Override
	public String getClock() {
		try {
			String clock = commandProcessor.execute(path + "/" + CLOCK_SCRIPT);
			logger.info("Getting actual hour: {}", clock);
			return clock;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Clock command '{}': {}", CLOCK_SCRIPT,
					ex);
			return null;
		}
	}

	@Override
	public long getUptime() {
		try {
			long uptime = Long.parseLong(commandProcessor.execute(path + "/" + UPTIME_SCRIPT));
			logger.info("Getting actual UpTime: {}", uptime);
			return uptime;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing UpTime command '{}': {}", UPTIME_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public int getTemperatureValue() {
		try {
			int temperatureValue = Integer.parseInt(commandProcessor.execute(path + "/" + TEMPERATURE_VALUE_SCRIPT));
			logger.info("Getting actual Temperature: {}", temperatureValue);
			return temperatureValue;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Temperature command '{}': {}", TEMPERATURE_VALUE_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public String getTemperatureStatus() {
		try {
			String temperatureStatus = commandProcessor.execute(path + "/" + TEMPERATURE_STATUS_SCRIPT);
			logger.info("Getting actual Temperature Status: {}", temperatureStatus);
			return temperatureStatus;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Temperature Status command '{}': {}", TEMPERATURE_STATUS_SCRIPT,
					ex);
			return null;
		}
	}

	@Override
	public String getCpuStatus() {
		try {
			String cpuStatus = commandProcessor.execute(path + "/" + CPU_STATUS_SCRIPT);
			logger.info("Getting actual CPU Status: {}", cpuStatus);
			return cpuStatus;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing CPU Status command '{}': {}", CPU_STATUS_SCRIPT,
					ex);
			return null;
		}
	}

	@Override
	public int getCpuUsage() {
		try {
			int cpuUsage = Integer.parseInt(commandProcessor.execute(path + "/" + CPU_USAGE_SCRIPT));
			logger.info("Getting actual CPU Usage: {}", cpuUsage);
			return cpuUsage;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing CPU Usage command '{}': {}", CPU_USAGE_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public int getCpuTotal() {
		try {
			int cpuTotal = Integer.parseInt(commandProcessor.execute(path + "/" + CPU_TOTAL_SCRIPT));
			logger.info("Getting actual cores quantity: {}", cpuTotal);
			return cpuTotal;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Clock command '{}': {}", CPU_TOTAL_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public int getRamUsage() {
		try {
			int ramUsage = Integer.parseInt(commandProcessor.execute(path + "/" + RAM_USAGE_SCRIPT));
			logger.info("Getting actual RAM Usage: {}", ramUsage);
			return ramUsage;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing RAM Usage command '{}': {}", RAM_USAGE_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public long getRamTotal() {
		try {
			long ramTotal = Long.parseLong(commandProcessor.execute(path + "/" + RAM_TOTAL_SCRIPT));
			logger.info("Getting actual RAM Usage: {}", ramTotal);
			return ramTotal;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing RAM Total command '{}': {}", RAM_TOTAL_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public int getDiskUsage() {
		try {
			int diskUsage = Integer.parseInt(commandProcessor.execute(path + "/" + DISK_USAGE_SCRIPT));
			logger.info("Getting actual Disk Capacity Usage: {}", diskUsage);
			return diskUsage;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Disk Usage command '{}': {}", DISK_USAGE_SCRIPT,
					ex);
			return 0;
		}
	}

	@Override
	public long getDiskTotal() {
		try {
			long diskTotal = Long.parseLong(commandProcessor.execute(path + "/" + DISK_TOTAL_SCRIPT));
			logger.info("Getting actual Disk Capacity Usage: {}", diskTotal);
			return diskTotal;
		} catch (CommandExecutionException ex) {
			logger.error("Error executing Disk Total command '{}': {}", DISK_TOTAL_SCRIPT,
					ex);
			return 0;
		}
	}

	private Software parseSoftware(String value, String type) {
		String[] values = value.split(":");
		return new Software(values[0], values[1], type);
	}
}
