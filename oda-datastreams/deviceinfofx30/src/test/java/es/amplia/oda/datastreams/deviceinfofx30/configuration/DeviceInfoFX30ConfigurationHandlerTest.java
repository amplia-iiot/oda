package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.ScriptsLoader;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static es.amplia.oda.datastreams.deviceinfofx30.configuration.DeviceInfoFX30ConfigurationHandler.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
		List<List<?>> configurationArgs = new ArrayList<>();
		try (MockedConstruction<DeviceInfoFX30Configuration> configurationCons =
					 mockConstruction(DeviceInfoFX30Configuration.class,
							 (mock, mctx) -> configurationArgs.add(new ArrayList<>(mctx.arguments())))) {
			testHandler.loadConfiguration(props);

			assertEquals(1, configurationCons.constructed().size());
			assertEquals(TEST_DEVICE_ID, configurationArgs.get(0).get(0));
			assertEquals(TEST_API_KEY, configurationArgs.get(0).get(1));
			assertEquals(TEST_SOURCE, configurationArgs.get(0).get(2));
			assertEquals(TEST_PATH, configurationArgs.get(0).get(3));
		}
	}

	@Test
	public void  testLoadConfigurationMissingApiKey() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(SOURCE_PROPERTY_NAME, TEST_SOURCE);
		props.put(PATH_PROPERTY_NAME, TEST_PATH);

		assertThrows(ConfigurationException.class, () -> testHandler.loadConfiguration(props));
	}

	@Test
	public void testLoadConfigurationMissingSource() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(API_KEY_PROPERTY_NAME, TEST_API_KEY);
		props.put(PATH_PROPERTY_NAME, TEST_PATH);

		assertThrows(ConfigurationException.class, () -> testHandler.loadConfiguration(props));
	}

	@Test
	public void testLoadConfigurationMissingPath() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DEVICE_ID_PROPERTY_NAME, TEST_DEVICE_ID);
		props.put(API_KEY_PROPERTY_NAME, TEST_API_KEY);
		props.put(SOURCE_PROPERTY_NAME, TEST_SOURCE);

		assertThrows(ConfigurationException.class, () -> testHandler.loadConfiguration(props));
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

		assertTrue(true, "Command Execution Exception is caught");
	}

	@Test
	public void testApplyConfigurationIOExceptionCaught() throws CommandExecutionException, IOException {
		Whitebox.setInternalState(testHandler, "currentConfiguration", TEST_CONFIGURATION);

		doThrow(new IOException()).when(mockedScriptsLoader).loadScripts(anyString(), anyString(), anyString());

		testHandler.applyConfiguration();

		assertTrue(true, "IO Exception is caught");
	}
}
