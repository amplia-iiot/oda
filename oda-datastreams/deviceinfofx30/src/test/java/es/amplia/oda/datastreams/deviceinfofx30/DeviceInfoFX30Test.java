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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

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
	@Mock
	private File mockedFile;

	@Before
	public void beforeTests() {
		bundles = new Bundle[0];
		configuration = new DeviceInfoFX30Configuration("deviceId", "apiKey", "source", "path");

		deviceInfo = new DeviceInfoFX30(null, bundles);
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		bundles = new Bundle[1];
		bundles[0] = mockedBundle;
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "bundles", bundles);
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		when(mockedBundle.getSymbolicName()).thenReturn("MyName");
		when(mockedBundle.getVersion()).thenReturn(new Version("1"));
//		when(mockedCommandProcessor.execute("chmod +x path/*.sh")).thenReturn("");
		when(mockedFile.listFiles()).thenReturn(new File[0]);
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.SERIAL_NUMBER_SCRIPT)).thenReturn("serialNumber");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.MODEL_SCRIPT)).thenReturn("model");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.IMEI_SCRIPT)).thenReturn("imei");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.IMSI_SCRIPT)).thenReturn("imsi");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.ICC_SCRIPT)).thenReturn("icc");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RSSI_SCRIPT)).thenReturn("rssi");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.SOFTWARE_SCRIPT)).thenReturn(" :  && Firmware Version:v");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.APN_SCRIPT)).thenReturn("apn");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_TOTAL_SCRIPT)).thenReturn("1000");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RAM_TOTAL_SCRIPT)).thenReturn("2000");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.DISK_TOTAL_SCRIPT)).thenReturn("4000");

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
		assertEquals(1000L, deviceInfo.getCpuTotal());
		assertEquals(2000L, deviceInfo.getRamTotal());
		assertEquals(4000L, deviceInfo.getDiskTotal());
	}

	@Test
	public void testLoadConfigurationException() throws Exception {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "bundles", bundles);
		when(mockedCommandProcessor.execute(any())).thenThrow(mockedCommandExecutionException);
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		when(mockedFile.listFiles()).thenReturn(new File[0]);

		deviceInfo.loadConfiguration(configuration);

		assertNull(deviceInfo.getSerialNumber());
		assertNull(deviceInfo.getModel());
		assertNull(deviceInfo.getImei());
		assertNull(deviceInfo.getImsi());
		assertNull(deviceInfo.getIcc());
		assertNull(deviceInfo.getRssi());
		assertNull(deviceInfo.getSoftware());
		assertNull(deviceInfo.getApn());
		assertEquals(0, deviceInfo.getCpuTotal());
		assertEquals(0, deviceInfo.getRamTotal());
		assertEquals(0, deviceInfo.getDiskTotal());
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
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.UPTIME_SCRIPT)).thenReturn("200");

		long uptime = deviceInfo.getUptime();

		assertEquals(200, uptime);
	}

	@Test
	public void testGetUptimeException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.UPTIME_SCRIPT)).thenThrow(mockedCommandExecutionException);

		long uptime = deviceInfo.getUptime();

		assertEquals(0, uptime);
	}

	@Test
	public void testGetTemperatureValue() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.TEMPERATURE_VALUE_SCRIPT)).thenReturn("42");

		int temperatureValue = deviceInfo.getTemperatureValue();

		assertEquals(42, temperatureValue);
	}

	@Test
	public void testGetTemperatureValueException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.TEMPERATURE_VALUE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		int temperatureValue = deviceInfo.getTemperatureValue();

		assertEquals(0, temperatureValue);
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

		int cpuUsage = deviceInfo.getCpuUsage();

		assertEquals(42, cpuUsage);
	}

	@Test
	public void testGetCpuUsageException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.CPU_USAGE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		int cpuUsage = deviceInfo.getCpuUsage();

		assertEquals(0, cpuUsage);
	}

	@Test
	public void testGetRamUsage() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RAM_USAGE_SCRIPT)).thenReturn("42");

		int ramUsage = deviceInfo.getRamUsage();

		assertEquals(42, ramUsage);
	}

	@Test
	public void testGetRamUsageException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.RAM_USAGE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		int ramUsage = deviceInfo.getRamUsage();

		assertEquals(0, ramUsage);
	}

	@Test
	public void testGetDiskUsage() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.DISK_USAGE_SCRIPT)).thenReturn("42");

		int diskUsage = deviceInfo.getDiskUsage();

		assertEquals(42, diskUsage);
	}

	@Test
	public void testGetDiskUsageException() throws CommandExecutionException {
		Whitebox.setInternalState(deviceInfo, "commandProcessor", mockedCommandProcessor);
		Whitebox.setInternalState(deviceInfo, "path", "path");
		when(mockedCommandProcessor.execute("path/" + DeviceInfoFX30.DISK_USAGE_SCRIPT)).thenThrow(mockedCommandExecutionException);

		int diskUsage = deviceInfo.getDiskUsage();

		assertEquals(0, diskUsage);
	}
}
