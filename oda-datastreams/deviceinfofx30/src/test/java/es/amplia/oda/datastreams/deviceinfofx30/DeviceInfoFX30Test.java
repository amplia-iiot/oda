package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.entities.Software;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeviceInfoFX30.class)
public class DeviceInfoFX30Test {

	private DeviceInfoFX30 deviceInfo;
	private Bundle[] bundles;
	private DeviceInfoFX30Configuration configuration;

	@Mock
	private CommandProcessor mockedCommandProcessor;
	@Mock
	private CommandExecutionException mockedCommandExecutionException;
	@Mock
	private Bundle mockedBundle;

	@Before
	public void beforeTests() {
		bundles = new Bundle[0];
		configuration = new DeviceInfoFX30Configuration("deviceId", "apiKey", "path");

		deviceInfo = new DeviceInfoFX30(null, bundles);
	}

	@Test
	public void testLoadConfiguration() throws CommandExecutionException {
		bundles = new Bundle[1];
		bundles[0] = mockedBundle;
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "bundles", bundles);
		when(mockedBundle.getSymbolicName()).thenReturn("MyName");
		when(mockedBundle.getVersion()).thenReturn(new Version("1"));
		when(mockedCommandProcessor.execute("chmod +x path/*.sh")).thenReturn("");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.SERIAL_NUMBER_SCRIPT)).thenReturn("serialNumber");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.MODEL_SCRIPT)).thenReturn("model");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.IMEI_SCRIPT)).thenReturn("imei");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.IMSI_SCRIPT)).thenReturn("imsi");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.ICC_SCRIPT)).thenReturn("icc");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RSSI_SCRIPT)).thenReturn("rssi");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.SOFTWARE_SCRIPT)).thenReturn(" :  && Firmware Version:v");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.APN_SCRIPT)).thenReturn("apn");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_TOTAL_SCRIPT)).thenReturn("cpuCap");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RAM_TOTAL_SCRIPT)).thenReturn("ramCap");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.DISK_TOTAL_SCRIPT)).thenReturn("diskCap");

		deviceInfo.loadConfiguration(configuration);

		assertEquals("deviceId", deviceInfo.getDeviceId());
		assertEquals("apiKey", deviceInfo.getApiKey());
		assertEquals("path", deviceInfo.getPath());
		assertEquals("serialNumber", deviceInfo.getSerialNumber());
		assertEquals("model", deviceInfo.getModel());
		assertEquals("imei", deviceInfo.getImei());
		assertEquals("imsi", deviceInfo.getImsi());
		assertEquals("icc", deviceInfo.getIcc());
		assertEquals("rssi", deviceInfo.getRssi());
		List<Software> list = new ArrayList<>();
		list.add(new Software(" "," ","SOFTWARE"));
		list.add(new Software("Firmware Version","v","FIRMWARE"));
		list.add(new Software("MyName","1.0.0","SOFTWARE"));
		assertEquals(list, deviceInfo.getSoftware());
		assertEquals("apn", deviceInfo.getApn());
		assertEquals("cpuCap", deviceInfo.getCpuTotal());
		assertEquals("ramCap", deviceInfo.getRamTotal());
		assertEquals("diskCap", deviceInfo.getDiskTotal());
	}

	@Test
	public void testLoadConfigurationException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "bundles", bundles);
		when(mockedCommandProcessor.execute("chmod +x path/*.sh")).thenThrow(mockedCommandExecutionException);

		deviceInfo.loadConfiguration(configuration);

		assertNull(deviceInfo.getSerialNumber());
		assertNull(deviceInfo.getModel());
		assertNull(deviceInfo.getImei());
		assertNull(deviceInfo.getImsi());
		assertNull(deviceInfo.getIcc());
		assertNull(deviceInfo.getRssi());
		assertNull(deviceInfo.getSoftware());
		assertNull(deviceInfo.getApn());
		assertNull(deviceInfo.getCpuTotal());
		assertNull(deviceInfo.getRamTotal());
		assertNull(deviceInfo.getDiskTotal());
	}

	@Test
	public void testGetClock() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CLOCK_SCRIPT)).thenReturn("clock");

		String clock = deviceInfo.getClock();

		assertEquals("clock", clock);
	}

	@Test
	public void testGetClockException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CLOCK_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String clock = deviceInfo.getClock();

		assertNull(clock);
	}

	@Test
	public void testGetUptime() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.UPTIME_SCRIPT)).thenReturn("uptime");

		String uptime = deviceInfo.getUptime();

		assertEquals("uptime", uptime);
	}

	@Test
	public void testGetUptimeException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.UPTIME_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String uptime = deviceInfo.getUptime();

		assertNull(uptime);
	}

	@Test
	public void testGetTemperatureValue() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.TEMPERATURE_VALUE_SCRIPT)).thenReturn("42");

		String temperatureValue = deviceInfo.getTemperatureValue();

		assertEquals("42", temperatureValue);
	}

	@Test
	public void testGetTemperatureValueException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.TEMPERATURE_VALUE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String temperatureValue = deviceInfo.getTemperatureValue();

		assertNull(temperatureValue);
	}

	@Test
	public void testGetTemperatureStatus() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.TEMPERATURE_STATUS_SCRIPT)).thenReturn("OK");

		String temperatureStatus = deviceInfo.getTemperatureStatus();

		assertEquals("OK", temperatureStatus);
	}

	@Test
	public void testGetTemperatureStatusException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.TEMPERATURE_STATUS_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String temperatureStatus = deviceInfo.getTemperatureStatus();

		assertNull(temperatureStatus);
	}

	@Test
	public void testGetCpuStatus() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_STATUS_SCRIPT)).thenReturn("OK");

		String cpu = deviceInfo.getCpuStatus();

		assertEquals("OK", cpu);
	}

	@Test
	public void testGetCpuStatusException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_STATUS_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String cpu = deviceInfo.getCpuStatus();

		assertNull(cpu);
	}

	@Test
	public void testGetCpuUsage() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_USAGE_SCRIPT)).thenReturn("42");

		String cpuUsage = deviceInfo.getCpuUsage();

		assertEquals("42", cpuUsage);
	}

	@Test
	public void testGetCpuUsageException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_USAGE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String cpuUsage = deviceInfo.getCpuUsage();

		assertNull(cpuUsage);
	}

	@Test
	public void testGetRamUsage() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RAM_USAGE_SCRIPT)).thenReturn("42");

		String ramUsage = deviceInfo.getRamUsage();

		assertEquals("42", ramUsage);
	}

	@Test
	public void testGetRamUsageException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RAM_USAGE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String ramUsage = deviceInfo.getRamUsage();

		assertNull(ramUsage);
	}

	@Test
	public void testGetDiskUsage() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.DISK_USAGE_SCRIPT)).thenReturn("42");

		String diskUsage = deviceInfo.getDiskUsage();

		assertEquals("42", diskUsage);
	}

	@Test
	public void testGetDiskUsageException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.DISK_USAGE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		String diskUsage = deviceInfo.getDiskUsage();

		assertNull(diskUsage);
	}

	@Test
	public void testGetIpPresence() {
		Whitebox.setInternalState(deviceInfo, "ipPresence", "No");

		String ipPresence = deviceInfo.getIpPresence();

		assertEquals("No", ipPresence);
	}

	@Test
	public void testGetIpAddress() {
		Whitebox.setInternalState(deviceInfo, "ipAddress", "0.0.0.0");

		String ipAddress = deviceInfo.getIpAddress();

		assertEquals("0.0.0.0", ipAddress);
	}
}
