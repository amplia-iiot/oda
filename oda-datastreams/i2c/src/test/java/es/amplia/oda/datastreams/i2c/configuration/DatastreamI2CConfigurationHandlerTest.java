package es.amplia.oda.datastreams.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.datastreams.i2c.I2CDatastreamsRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Dictionary;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DatastreamI2CConfigurationHandler.class, Collections.class, I2CDatastreamConfiguration.class})
public class DatastreamI2CConfigurationHandlerTest {

	private static final String TEST_DEVICE_NAME = "device";

	@Mock
	I2CDatastreamsRegistry mockedRegistry;
	@Mock
	I2CService mockedService;
	@InjectMocks
	private DatastreamI2CConfigurationHandler testHandler;
	@Mock
	Dictionary<String, String> mockedDictionary;
	@Mock
	I2CDevice mockedDevice;
	@Mock
	I2CDatastreamConfiguration mockedConfiguration;

	@Test
	public void testLoadConfiguration() {
		Map<Object, Object> map = java.util.Collections.singletonMap(TEST_DEVICE_NAME, "getter:true,setter:false");
		mockStatic(Collections.class);
		PowerMockito.when(Collections.dictionaryToMap(any())).thenReturn(map);

		testHandler.loadConfiguration(mockedDictionary);

		Map<String, I2CDatastreamConfiguration> datastreams = (Map<String, I2CDatastreamConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertTrue(datastreams.containsKey(TEST_DEVICE_NAME));
	}

	@Test
	public void testLoadConfigurationWithException() {
		Map<Object, Object> map = java.util.Collections.singletonMap(TEST_DEVICE_NAME, null);
		mockStatic(Collections.class);
		when(Collections.dictionaryToMap(any())).thenReturn(map);

		testHandler.loadConfiguration(mockedDictionary);
	}

	@Test
	public void testLoadDefaultConfiguration() {
		when(mockedService.getAllI2C()).thenReturn(java.util.Collections.singletonList(mockedDevice));
		when(mockedDevice.getName()).thenReturn(TEST_DEVICE_NAME);

		testHandler.loadDefaultConfiguration();

		Map<String, I2CDatastreamConfiguration> datastreams = (Map<String, I2CDatastreamConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertTrue(datastreams.containsKey(TEST_DEVICE_NAME));
	}

	@Test
	public void testLoadDefaultConfigurationWithException() {
		when(mockedService.getAllI2C()).thenReturn(java.util.Collections.singletonList(mockedDevice));
		when(mockedDevice.getName()).thenReturn("name");
		mockStatic(I2CDatastreamConfiguration.class);
		when(I2CDatastreamConfiguration.builder()).thenThrow(new NullPointerException(""));

		testHandler.loadDefaultConfiguration();

		Map<String, I2CDatastreamConfiguration> datastreams = (Map<String, I2CDatastreamConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertTrue(datastreams.isEmpty());
	}

	@Test
	public void testApplyConfiguration() {
		Map<String, I2CDatastreamConfiguration> datastreams = java.util.Collections.singletonMap(TEST_DEVICE_NAME, mockedConfiguration);
		Whitebox.setInternalState(testHandler, "currentConfiguration", datastreams);
		when(mockedConfiguration.isGetter()).thenReturn(true);
		when(mockedConfiguration.isSetter()).thenReturn(true);

		testHandler.applyConfiguration();

		verify(mockedRegistry).close();
	}
}