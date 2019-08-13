package es.amplia.oda.core.commons.interfaces;

public interface DeviceInfoFX30Provider {
	String getDeviceId(); //Must return the device Id of ODA
	String getApiKey();
	String getMaker();
	String getModel();
	String getImei();
	String getImsi();
	String getIcc();
	String getRssi();
	String getSoftware();
	String getIpPresence();
	String getIpAddress();
	String getApn();
}
