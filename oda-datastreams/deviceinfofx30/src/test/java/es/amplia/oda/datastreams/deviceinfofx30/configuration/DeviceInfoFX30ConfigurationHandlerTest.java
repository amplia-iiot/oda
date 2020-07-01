package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.ScriptsLoader;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30ConfigurationHandler.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeviceInfoFX30ConfigurationHandler.class)
public class DeviceInfoFX30ConfigurationHandlerTest {

	private static final String TEST_DEVICE_ID = "deviceId";
	private static final String TEST_API_KEY = "apiKey";
	private static final String TEST_SOURCE = "source";
	private static final String TEST_PATH = "path";
	private static final DeviceInfoFX30Configuration TEST_CONFIGURATION =
			new DeviceInfoFX30Configuration(TEST_DEVICE_ID, TEST_API_KEY, TEST_SOURCE, TEST_PATH);

	@Mock
	private ScriptsLoader mockedScriptsLoader;
	@Mock
	private DeviceInfoFX30 mockedDeviceInfo;
	@InjectMocks
	private DeviceInfoFX30ConfigurationHandler testHandler;


	@Test
	public void testLoadConfiguration() throws Exception {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(API_KEY_PROPERTY_NAME, TEST_API_KEY);
		props.put(SOURCE_PROPERTY_NAME, TEST_SOURCE);
		props.put(PATH_PROPERTY_NAME, TEST_PATH);
		whenNew(DeviceInfoFX30Configuration.class).withAnyArguments().thenReturn(TEST_CONFIGURATION);

		testHandler.loadConfiguration(props);

		verifyNew(DeviceInfoFX30Configuration.class)
				.withArguments(TEST_DEVICE_ID, TEST_API_KEY, TEST_SOURCE, TEST_PATH);
	}

	@Test(expected = ConfigurationException.class)
	public void  testLoadConfigurationMissingApiKey() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(SOURCE_PROPERTY_NAME, TEST_SOURCE);
		props.put(PATH_PROPERTY_NAME, TEST_PATH);

		testHandler.loadConfiguration(props);
	}

	@Test(expected = ConfigurationException.class)
	public void testLoadConfigurationMissingSource() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(API_KEY_PROPERTY_NAME, TEST_API_KEY);
		props.put(PATH_PROPERTY_NAME, TEST_PATH);

		testHandler.loadConfiguration(props);
	}

	@Test(expected = ConfigurationException.class)
	public void testLoadConfigurationMissingPath() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(API_KEY_PROPERTY_NAME, TEST_API_KEY);
		props.put(SOURCE_PROPERTY_NAME, TEST_SOURCE);

		testHandler.loadConfiguration(props);
	}

	@Test
	public void testApplyConfiguration() throws CommandExecutionException, IOException {
		Whitebox.setInternalState(testHandler, "currentConfiguration", TEST_CONFIGURATION);

		testHandler.applyConfiguration();

		verify(mockedScriptsLoader).loadScripts(eq(TEST_SOURCE), eq(TEST_PATH), eq(BUNDLE_ARTIFACT_ID));
		verify(mockedDeviceInfo).loadConfiguration(eq(TEST_CONFIGURATION));
	}

	@Test
	public void testApplyConfigurationCommandExecutionExceptionCaught() throws CommandExecutionException, IOException {
		Whitebox.setInternalState(testHandler, "currentConfiguration", TEST_CONFIGURATION);

		doThrow(new CommandExecutionException("","",null)).when(mockedScriptsLoader)
				.loadScripts(anyString(), anyString(), anyString());

		testHandler.applyConfiguration();

		assertTrue("Command Execution Exception is caught", true);
	}

	@Test
	public void testApplyConfigurationIOExceptionCaught() throws CommandExecutionException, IOException {
		Whitebox.setInternalState(testHandler, "currentConfiguration", TEST_CONFIGURATION);

		doThrow(new IOException()).when(mockedScriptsLoader).loadScripts(anyString(), anyString(), anyString());

		testHandler.applyConfiguration();

		assertTrue("IO Exception is caught", true);
	}
}
