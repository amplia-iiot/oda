package es.amplia.oda.datastreams.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.datastreams.i2c.I2CDatastreamsRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatastreamI2CConfigurationHandlerTest {

	private static final String TEST_DEVICE_1 = "deviceWithGetAndSet";
	private static final long TEST_MIN_1 = 1;
	private static final long TEST_MAX_1 = 50;
	private static final String TEST_DEVICE_2 = "deviceWithSetAndDefaultGet";
	private static final String TEST_DEVICE_3 = "defaultWithGetAndDefaultSet";
	private static final String TEST_DEFAULT_DEVICE_NAME = "defaultDeviceName";
	private static final String TEST_INVALID_1 = "minGreaterThanMax";
	private static final String TEST_INVALID_2 = "falseGetterAndSetter";

	private static final I2CDatastreamsConfiguration TEST_CONFIGURATION = I2CDatastreamsConfiguration.builder()
			.name(TEST_DEVICE_1).device(TEST_DEFAULT_DEVICE_NAME).min(TEST_MIN_1).max(TEST_MAX_1).getter(true).setter(true).build();


	@Mock
	private I2CDatastreamsRegistry mockedRegistry;
	@Mock
	private I2CService mockedService;
	@InjectMocks
	private DatastreamI2CConfigurationHandler testHandler;

	@Mock
	private I2CDevice mockedDevice;


	@Test
	public void testLoadConfiguration() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(TEST_DEVICE_1, "device:" + TEST_DEFAULT_DEVICE_NAME + ", min:" + TEST_MIN_1 + ", max:" + TEST_MAX_1 + ", getter:false,setter:true");
		props.put(TEST_DEVICE_2, "device:" + TEST_DEFAULT_DEVICE_NAME + ", setter:true");
		props.put(TEST_DEVICE_3, "device:" + TEST_DEFAULT_DEVICE_NAME + ", getter:true");
		props.put(TEST_INVALID_1, "min:40,max:1");
		props.put(TEST_INVALID_2, "getter:false,setter:false");

		testHandler.loadConfiguration(props);

		Map<String, I2CDatastreamsConfiguration> datastreams = getCurrentConfiguration();
		assertTrue(datastreams.containsKey(TEST_DEVICE_1));
		I2CDatastreamsConfiguration configuration = datastreams.get(TEST_DEVICE_1);
		assertNotNull(configuration);
		assertEquals(TEST_DEVICE_1, configuration.getName());
		assertEquals(TEST_DEFAULT_DEVICE_NAME, configuration.getDevice());
		assertEquals(TEST_MIN_1, configuration.getMin());
		assertEquals(TEST_MAX_1, configuration.getMax());
		assertFalse(configuration.isGetter());
		assertTrue(configuration.isSetter());
		assertTrue(datastreams.containsKey(TEST_DEVICE_2));
		configuration = datastreams.get(TEST_DEVICE_2);
		assertNotNull(configuration);
		assertEquals(TEST_DEVICE_2, configuration.getName());
		assertEquals(TEST_DEFAULT_DEVICE_NAME, configuration.getDevice());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_MIN, configuration.getMin());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_MAX, configuration.getMax());
		assertTrue(configuration.isGetter());
		assertTrue(configuration.isSetter());
		assertTrue(datastreams.containsKey(TEST_DEVICE_3));
		configuration = datastreams.get(TEST_DEVICE_3);
		assertNotNull(configuration);
		assertEquals(TEST_DEVICE_3, configuration.getName());
		assertEquals(TEST_DEFAULT_DEVICE_NAME, configuration.getDevice());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_MIN, configuration.getMin());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_MAX, configuration.getMax());
		assertTrue(configuration.isGetter());
		assertFalse(configuration.isSetter());
		assertFalse(datastreams.containsKey(TEST_INVALID_1));
		assertFalse(datastreams.containsKey(TEST_INVALID_2));
	}

	@SuppressWarnings("unchecked")
	private Map<String, I2CDatastreamsConfiguration> getCurrentConfiguration() {
		return (Map<String, I2CDatastreamsConfiguration>) Whitebox.getInternalState(testHandler, "currentConfiguration");
	}

	@Test
	public void testLoadDefaultConfiguration() {
		when(mockedService.getAllI2C()).thenReturn(Collections.singletonList(mockedDevice));
		when(mockedDevice.getName()).thenReturn(TEST_DEVICE_1);

		testHandler.loadDefaultConfiguration();

		Map<String, I2CDatastreamsConfiguration> datastreams = getCurrentConfiguration();
		assertTrue(datastreams.containsKey(TEST_DEVICE_1));
		I2CDatastreamsConfiguration configuration = datastreams.get(TEST_DEVICE_1);
		assertNotNull(configuration);
		assertEquals(TEST_DEVICE_1, configuration.getName());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_MIN, configuration.getMin());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_MAX, configuration.getMax());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_GETTER, configuration.isGetter());
		assertEquals(I2CDatastreamsConfiguration.DEFAULT_SETTER, configuration.isSetter());
	}

	@Test
	public void testApplyConfiguration() {
		Map<String, I2CDatastreamsConfiguration> datastreams = Collections.singletonMap(TEST_DEVICE_1, TEST_CONFIGURATION);

		Whitebox.setInternalState(testHandler, "currentConfiguration", datastreams);

		testHandler.applyConfiguration();

		verify(mockedRegistry).close();
		verify(mockedRegistry).addDatastreamGetter(eq(TEST_DEVICE_1), eq(TEST_DEFAULT_DEVICE_NAME) , eq(TEST_MIN_1), eq(TEST_MAX_1));
		verify(mockedRegistry).addDatastreamSetter(eq(TEST_DEVICE_1));
	}
}