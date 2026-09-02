package es.amplia.oda.hardware.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.hardware.i2c.DioZeroI2CDevice;
import es.amplia.oda.hardware.i2c.DioZeroI2CService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DioZeroI2CConfigurationHandlerTest {

	private static final String TEST_DEVICE_NAME = "device";

	@Mock
	DioZeroI2CService mockedService;
	@InjectMocks
	DioZeroI2CConfigurationHandler testHandler;
	@Mock
	Dictionary<String, String> mockedDictionary;

	@Test
	public void loadConfiguration() throws Exception {
		Map<Object, Object> map = java.util.Collections.singletonMap(TEST_DEVICE_NAME, "controller:0,address:104,register:160,min:0,max:100");
		List<List<?>> deviceArgs = new ArrayList<>();

		try (MockedStatic<Collections> mockedCollections = mockStatic(Collections.class);
			 MockedConstruction<com.diozero.api.I2CDevice> directionCons =
					 mockConstruction(com.diozero.api.I2CDevice.class);
			 MockedConstruction<DioZeroI2CDevice> deviceCons = mockConstruction(DioZeroI2CDevice.class,
					 (mock, mctx) -> deviceArgs.add(new ArrayList<>(mctx.arguments())))) {
			mockedCollections.when(() -> Collections.dictionaryToMap(any())).thenReturn(map);

			testHandler.loadConfiguration(mockedDictionary);

			List<I2CDevice> devices = getConfiguredDevices();
			assertEquals(1, deviceCons.constructed().size());
			assertEquals(TEST_DEVICE_NAME, deviceArgs.get(0).get(0));
			assertEquals(160, deviceArgs.get(0).get(1));
			assertEquals(directionCons.constructed().get(0), deviceArgs.get(0).get(2));
			assertEquals(0., deviceArgs.get(0).get(3));
			assertEquals(100., deviceArgs.get(0).get(4));
			assertEquals(1, devices.size());
		}
	}

	@SuppressWarnings("unchecked")
	private List<I2CDevice> getConfiguredDevices() {
		return (List<I2CDevice>) Whitebox.getInternalState(testHandler, "configuredDevices");
	}

	@Test
	public void loadConfigurationWithAnException() throws Exception {
		Map<Object, Object> map = java.util.Collections.singletonMap(TEST_DEVICE_NAME, null);

		try (MockedStatic<Collections> mockedCollections = mockStatic(Collections.class);
			 MockedConstruction<DioZeroI2CDevice> deviceCons = mockConstruction(DioZeroI2CDevice.class)) {
			mockedCollections.when(() -> Collections.dictionaryToMap(any())).thenReturn(map);

			testHandler.loadConfiguration(mockedDictionary);

			List<I2CDevice> devices = getConfiguredDevices();
			assertEquals(0, deviceCons.constructed().size());
			assertEquals(0, devices.size());
		}
	}

	@Test
	public void applyConfiguration() {
		testHandler.applyConfiguration();

		verify(mockedService).loadConfiguration(eq(new ArrayList<>()));
	}
}
