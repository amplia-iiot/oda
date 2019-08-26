package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Dictionary;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeviceInfoFX30ConfigurationHandler.class)
public class DeviceInfoFX30ConfigurationHandlerTest {

	@Mock
	public DeviceInfoFX30 mockedDeviceInfoFX30;

	@InjectMocks
	private DeviceInfoFX30ConfigurationHandler testHandler;

	@Mock
	public Dictionary mockedDictionary;

	@Test
	public void testLoadConfiguration() throws Exception {
		DeviceInfoFX30Configuration configuration = new DeviceInfoFX30Configuration("deviceId", "apiKey", "path");
		whenNew(DeviceInfoFX30Configuration.class).withAnyArguments().thenReturn(configuration);
		when(mockedDictionary.get(eq("deviceId"))).thenReturn("deviceId");
		when(mockedDictionary.get(eq("apiKey"))).thenReturn("apiKey");
		when(mockedDictionary.get(eq("path"))).thenReturn("path");

		testHandler.loadConfiguration(mockedDictionary);

		verifyNew(DeviceInfoFX30Configuration.class).withArguments("deviceId", "apiKey", "path");
	}

	@Test(expected = ConfigurationException.class)
	public void  testLoadConfigurationApiNull() {
		when(mockedDictionary.get(eq("deviceId"))).thenReturn("deviceId");
		when(mockedDictionary.get(eq("apiKey"))).thenReturn(null);
		when(mockedDictionary.get(eq("path"))).thenReturn("path");

		testHandler.loadConfiguration(mockedDictionary);
	}

	@Test(expected = ConfigurationException.class)
	public void testLoadConfigurationPathNull() {
		when(mockedDictionary.get(eq("deviceId"))).thenReturn("deviceId");
		when(mockedDictionary.get(eq("apiKey"))).thenReturn("apiKey");
		when(mockedDictionary.get(eq("path"))).thenReturn(null);

		testHandler.loadConfiguration(mockedDictionary);
	}

	@Test
	public void testApplyConfiguration() {
		DeviceInfoFX30Configuration configuration = new DeviceInfoFX30Configuration("deviceId", "apiKey", "path");
		Whitebox.setInternalState(testHandler, "currentConfiguration", configuration);

		testHandler.applyConfiguration();

		verify(mockedDeviceInfoFX30).loadConfiguration(eq(configuration));
	}

}
