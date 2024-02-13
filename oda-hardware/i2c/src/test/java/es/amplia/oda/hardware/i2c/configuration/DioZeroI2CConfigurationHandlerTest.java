package es.amplia.oda.hardware.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.hardware.i2c.DioZeroI2CDevice;
import es.amplia.oda.hardware.i2c.DioZeroI2CService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DioZeroI2CConfigurationHandler.class,Collections.class})
@PowerMockIgnore("jdk.internal.reflect.*")
public class DioZeroI2CConfigurationHandlerTest {

	private static final String TEST_DEVICE_NAME = "device";

	@Mock
	DioZeroI2CService mockedService;
	@InjectMocks
	DioZeroI2CConfigurationHandler testHandler;
	@Mock
	Dictionary<String, String> mockedDictionary;
	@Mock
	DioZeroI2CDevice mockedDevice;
	@Mock
	com.diozero.api.I2CDevice mockedDirection;

	@Test
	public void loadConfiguration() throws Exception {
		Map<Object, Object> map = java.util.Collections.singletonMap(TEST_DEVICE_NAME, "controller:0,address:104,register:160,min:0,max:100");
		PowerMockito.mockStatic(Collections.class);
		PowerMockito.when(Collections.dictionaryToMap(any())).thenReturn(map);
		whenNew(com.diozero.api.I2CDevice.class).withAnyArguments().thenReturn(mockedDirection);
		whenNew(DioZeroI2CDevice.class).withAnyArguments().thenReturn(mockedDevice);

		testHandler.loadConfiguration(mockedDictionary);

		List<I2CDevice> devices = getConfiguredDevices();
		verifyNew(DioZeroI2CDevice.class).withArguments(eq(TEST_DEVICE_NAME), eq(160), eq(mockedDirection), eq(0.), eq (100.));
		assertEquals(1, devices.size());
	}

	@SuppressWarnings("unchecked")
	private List<I2CDevice> getConfiguredDevices() {
		return (List<I2CDevice>) Whitebox.getInternalState(testHandler, "configuredDevices");
	}

	@Test
	public void loadConfigurationWithAnException() throws Exception {
		Map<Object, Object> map = java.util.Collections.singletonMap(TEST_DEVICE_NAME, null);
		PowerMockito.mockStatic(Collections.class);
		PowerMockito.when(Collections.dictionaryToMap(any())).thenReturn(map);
		whenNew(DioZeroI2CDevice.class).withAnyArguments().thenReturn(mockedDevice);

		testHandler.loadConfiguration(mockedDictionary);

		List<I2CDevice> devices = getConfiguredDevices();
		assertEquals(0, devices.size());
	}

	@Test
	public void applyConfiguration() {
		testHandler.applyConfiguration();

		verify(mockedService).loadConfiguration(eq(new ArrayList<>()));
	}
}