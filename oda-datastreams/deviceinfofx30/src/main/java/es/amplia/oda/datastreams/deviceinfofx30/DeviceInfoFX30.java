package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30Configuration;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;

public class DeviceInfoFX30 implements DeviceInfoProvider {

	private static final Logger logger = LoggerFactory.getLogger(DeviceInfoFX30.class);

	private final CommandProcessor commandProcessor;
	private final Bundle bundle;

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

	private static final String TEMP_FILE = "./temp.sh";
	private static final String MODEL_SCRIPT = "obtainModel.sh";
	private static final String IMEI_SCRIPT = "obtainImei.sh";
	private static final String IMSI_SCRIPT = "obtainImsi.sh";
	private static final String ICC_SCRIPT = "obtainIcc.sh";
	private static final String RSSI_SCRIPT = "obtainRssi.sh";
	private static final String SOFTWARE_SCRIPT = "obtainSoftware.sh";
	private static final String IP_PRESENCE_SCRIPT = "";
	private static final String IP_ADDRESS_SCRIPT = "";
	private static final String APN_SCRIPT = "obtainApn.sh";

	private String deviceId;
	private String apiKey;
	private String serialNumber;
	private String maker;
	private String model;
	private String imei;
	private String imsi;
	private String icc;
	private String rssi;
	private String software;
	private String ipPresence;
	private String ipAddress;
	private String apn;

	DeviceInfoFX30(CommandProcessor commandProcessor, Bundle bundle) {
		this.commandProcessor = commandProcessor;
		this.bundle = bundle;
	}

	public void loadConfiguration(DeviceInfoFX30Configuration configuration) {
		deviceId = configuration.getDeviceId();
		logger.info("Load new device identifier: {}", deviceId);
		apiKey = configuration.getApiKey();
		logger.info("Load new API key: {}", apiKey);
		maker = configuration.getMaker();
		logger.info("Load new maker: {}", maker);
		try {
			commandProcessor.execute("ls");
			logger.info("Preparing scripts for run");
			commandProcessor.execute("chmod +x scripts/*.sh");
		} catch (CommandExecutionException ex) {
			logger.error("Scripts couldn't be prepared for ran");
		}
		try {
			serialNumber = commandProcessor.execute(configuration.getSerialNumberCommand());
			logger.info("Load new serial number: {}", serialNumber);
		} catch (CommandExecutionException ex) {
			logger.error("Error executing serial number command '{}': {}", configuration.getSerialNumberCommand(),
					ex);
		}
		try {
			extractScript(MODEL_SCRIPT);
			model = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new device model: {}", model);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing device model command '{}': {}", MODEL_SCRIPT,
					ex);
		}
		try {
			extractScript(IMEI_SCRIPT);
			imei = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new IMEI: {}", imei);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing IMEI command '{}': {}", IMEI_SCRIPT,
					ex);
		}
		try {
			extractScript(IMSI_SCRIPT);
			imsi = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new IMSI: {}", imsi);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing IMSI command '{}': {}", IMSI_SCRIPT,
					ex);
		}
		try {
			extractScript(ICC_SCRIPT);
			icc = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new ICC: {}", icc);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing ICC command '{}': {}", ICC_SCRIPT,
					ex);
		}
		try {
			extractScript(RSSI_SCRIPT);
			rssi = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new RSSI: {}", rssi);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing RSSI command '{}': {}", RSSI_SCRIPT,
					ex);
		}
		try {
			extractScript(SOFTWARE_SCRIPT);
			software = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new software version: {}", software);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing software version command '{}': {}", SOFTWARE_SCRIPT,
					ex);
		}
		/*try {
			ipPresence = commandProcessor.execute(IP_PRESENCE_SCRIPT);
			logger.info("Load new IP Presence: {}", ipPresence);
		} catch (CommandExecutionException ex) {
			logger.error("Error executing IP Presence command '{}': {}", IP_PRESENCE_SCRIPT,
					ex);
		}
		try {
			ipAddress = commandProcessor.execute(IP_ADDRESS_SCRIPT);
			logger.info("Load new IP Address: {}", ipAddress);
		} catch (CommandExecutionException ex) {
			logger.error("Error execlbuting IP Address command '{}': {}", IP_ADDRESS_SCRIPT,
					ex);
		}*/
		try {
			extractScript(APN_SCRIPT);
			apn = commandProcessor.execute(TEMP_FILE);
			deleteScript();
			logger.info("Load new APN: {}", apn);
		} catch (CommandExecutionException | IOException ex) {
			logger.error("Error executing APN command '{}': {}", APN_SCRIPT,
					ex);
		}
	}

	public String getSerialNumber() {
		return serialNumber;
	}
	@Override
	public String getDeviceId() {
		return deviceId != null ? deviceId : serialNumber;
	}

	@Override
	public String getApiKey() {
		return apiKey;
	}

	public String getMaker() {
		return maker;
	}

	public String getModel() {
		return model;
	}

	public String getImei() {
		return imei;
	}

	public String getImsi() {
		return imsi;
	}

	public String getIcc() {
		return icc;
	}

	public String getRssi() {
		return rssi;
	}

	public String getSoftware() {
		return software;
	}

	public String getIpPresence() {
		return ipPresence;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getApn() {
		return apn;
	}

	private void extractScript(String script) throws IOException, CommandExecutionException {
		URL url = this.bundle.getResource(script);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
		StringBuilder inputBuffer = new StringBuilder();
		String line;

		while ((line = br.readLine()) != null) {
			inputBuffer.append(line).append("\n");
		}
		br.close();
		FileOutputStream fileTemp = new FileOutputStream(TEMP_FILE);
		fileTemp.write(inputBuffer.toString().getBytes());
		fileTemp.close();

		commandProcessor.execute("chmod +x ./" + TEMP_FILE);
	}

	private boolean deleteScript() {
		File fileToDelete = new File(TEMP_FILE);
		return fileToDelete.delete();
	}
}
